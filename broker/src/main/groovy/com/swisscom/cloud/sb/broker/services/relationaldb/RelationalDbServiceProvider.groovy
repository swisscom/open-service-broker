/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.broker.services.relationaldb

import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.BindResponse
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.provisioning.ProvisionResponse
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import com.swisscom.cloud.sb.broker.util.StringGenerator
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailKey
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper
import groovy.util.logging.Log4j

@Log4j
abstract class RelationalDbServiceProvider implements ServiceProvider {
    public static final String MAX_CONNECTIONS = "max_connections"
    protected final RelationalDbClientFactory dbClientFactory
    protected final RelationalDbConfig dbConfig

    RelationalDbServiceProvider(RelationalDbConfig dbConfig, RelationalDbClientFactory dbClientFactory) {
        this.dbConfig = dbConfig
        this.dbClientFactory = dbClientFactory
    }

    @Override
    ProvisionResponse provision(ProvisionRequest request) {
        RelationalDbClient client = buildDbClient(request.serviceInstanceGuid)
        String database = createDatabaseName(request.serviceInstanceGuid)

        if (client.databaseExists(database)) {
            log.warn("Database ${database} already exists!")
            ErrorCode.RELATIONAL_DB_ALREADY_EXISTS.throwNew()
        }

        client.createDatabase(database)

        if (!client.databaseExists(database)) {
            log.warn("Database ${database} not created!")
            ErrorCode.SERVICEPROVIDER_INTERNAL_ERROR.throwNew()
        }

        return new ProvisionResponse(details: ServiceDetailsHelper.create().add(ServiceDetailKey.DATABASE, database).getDetails())
    }

    @Override
    DeprovisionResponse deprovision(DeprovisionRequest request) {
        String database = ServiceDetailsHelper.from(request.serviceInstance.details).getValue(ServiceDetailKey.DATABASE)
        RelationalDbClient client = buildDbClient(request.serviceInstanceGuid)
        if (!client.databaseExists(database)) {
            log.info("Database ${database} already did not exist!")
        } else {
            client.dropDatabase(database)

            if (client.databaseExists(database)) {
                log.warn("Database ${database} could not be deleted!")
                ErrorCode.SERVICEPROVIDER_INTERNAL_ERROR.throwNew()
            }
        }
        new DeprovisionResponse(isAsync: false)
    }

    @Override
    BindResponse bind(BindRequest request) {
        RelationalDbClient client = buildDbClient(request.serviceInstance.guid)
        String database = ServiceDetailsHelper.from(request.serviceInstance.details).getValue(ServiceDetailKey.DATABASE)
        dbShouldExist(client, database)

        String user = StringGenerator.randomAlphaNumericOfLength16()
        userShouldNotExist(client, user)

        String password = StringGenerator.randomAlphaNumericOfLength16()

        log.info("About to create user:${user} and grant rights on database:${database}")
        RelationalDbBindResponseDto credentials = client.createUserAndGrantRights(database, user, password, getMaxConnections(request))
        if (!client.userExists(user)) {
            log.warn("User ${user} could not be created!")
            ErrorCode.SERVICEPROVIDER_INTERNAL_ERROR.throwNew()
        }

        Collection<ServiceDetail> details = ServiceDetailsHelper.create()
                .add(ServiceDetailKey.USER, user)
                .add(ServiceDetailKey.PASSWORD, password)
                .getDetails()

        return new BindResponse(details: details, credentials: credentials)
    }

    private int getMaxConnections(BindRequest request) {
        def param = request.plan.parameters.find { it.name == MAX_CONNECTIONS }
        return param ? (param.value as int) : 0
    }

    private void dbShouldExist(RelationalDbClient client, String database) {
        boolean databaseExists = client.databaseExists(database)
        if (!databaseExists) {
            log.warn("Database ${database} not found!")
            ErrorCode.RELATIONAL_DB_NOT_FOUND.throwNew()
        }
    }

    private void userShouldNotExist(RelationalDbClient client, String user) {
        boolean userExists = client.userExists(user)
        if (userExists) {
            log.warn("User ${user} already exists!")
            ErrorCode.RELATIONAL_DB_USER_ALREADY_EXISTS.throwNew()
        }
    }

    @Override
    void unbind(UnbindRequest request) {
        RelationalDbClient client = buildDbClient(request.serviceInstance.guid)
        String database = ServiceDetailsHelper.from(request.serviceInstance.details).getValue(ServiceDetailKey.DATABASE)

        String user = ServiceDetailsHelper.from(request.binding.details).getValue(ServiceDetailKey.USER)
        if (!client.userExists(user)) {
            log.warn("User ${user} does not exist!")
        } else {
            client.revokeRightsAndDropUser(database, user)
            if (client.userExists(user)) {
                log.warn("User ${user} could not be deleted!")
                ErrorCode.SERVICEPROVIDER_INTERNAL_ERROR.throwNew()
            }
        }
    }

    protected RelationalDbClient buildDbClient(String serviceInstanceGuid) {
        def port = dbConfig.port ? dbConfig.port.toInteger() : 0
        return dbClientFactory.build(dbConfig.host, port, dbConfig.adminUser, dbConfig.adminPassword)
    }

    protected String createDatabaseName(String serviceInstanceId) {
        String databaseName = dbConfig.databasePrefix + serviceInstanceId;
        databaseName = databaseName.toUpperCase().replaceAll("-", "_");
        return databaseName
    }
}
