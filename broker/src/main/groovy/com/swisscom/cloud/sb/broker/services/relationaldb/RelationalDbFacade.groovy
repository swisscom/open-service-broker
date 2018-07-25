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
import com.swisscom.cloud.sb.broker.util.StringGenerator
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailKey
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component

@Component
@Slf4j
class RelationalDbFacade {
    public static final String MAX_CONNECTIONS = "max_connections"

    ProvisionResponse provision(RelationalDbClient client, ProvisionRequest request, String databasePrefix = "CFDB_") {
        String database = createDatabaseName(request.serviceInstanceGuid, databasePrefix)

        if (client.databaseExists(database)) {
            log.warn("Database ${database} already exists!")
            ErrorCode.RELATIONAL_DB_ALREADY_EXISTS.throwNew()
        }

        client.createDatabase(database)

        if (!client.databaseExists(database)) {
            log.warn("Database ${database} not created!")
            ErrorCode.SERVICEPROVIDER_INTERNAL_ERROR.throwNew()
        }
        new ProvisionResponse(details: ServiceDetailsHelper.create().add(ServiceDetailKey.DATABASE, database).getDetails())
    }

    DeprovisionResponse deprovision(RelationalDbClient client, DeprovisionRequest request) {
        String database = ServiceDetailsHelper.from(request.serviceInstance.details).getValue(ServiceDetailKey.DATABASE)
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

    BindResponse bind(RelationalDbClient client, BindRequest request) {
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

    void unbind(RelationalDbClient client, UnbindRequest request) {
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

    static String createDatabaseName(String serviceInstanceId, String databasePrefix = "") {
        String databaseName = databasePrefix + serviceInstanceId
        databaseName.toUpperCase().replaceAll("-", "_")
    }
}
