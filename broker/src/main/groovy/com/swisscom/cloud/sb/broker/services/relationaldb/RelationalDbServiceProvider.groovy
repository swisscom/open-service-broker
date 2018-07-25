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
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.provisioning.ProvisionResponse
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import groovy.util.logging.Log4j

@Log4j
abstract class RelationalDbServiceProvider implements ServiceProvider {
    protected final RelationalDbClientFactory dbClientFactory
    protected final RelationalDbConfig dbConfig

    RelationalDbFacade relationalDbFacade

    RelationalDbServiceProvider(RelationalDbConfig dbConfig, RelationalDbClientFactory dbClientFactory) {
        this.dbConfig = dbConfig
        this.dbClientFactory = dbClientFactory
        this.relationalDbFacade = new RelationalDbFacade()
    }

    @Override
    ProvisionResponse provision(ProvisionRequest request) {
        RelationalDbClient client = buildDbClient(request.serviceInstanceGuid)
        relationalDbFacade.provision(client, request)
    }

    @Override
    DeprovisionResponse deprovision(DeprovisionRequest request) {
        RelationalDbClient client = buildDbClient(request.serviceInstanceGuid)
        relationalDbFacade.deprovision(client, request)
    }

    @Override
    BindResponse bind(BindRequest request) {
        RelationalDbClient client = buildDbClient(request.serviceInstance.guid)
        relationalDbFacade.bind(client, request)
    }

    @Override
    void unbind(UnbindRequest request) {
        RelationalDbClient client = buildDbClient(request.serviceInstance.guid)
        relationalDbFacade.unbind(client, request)
    }

    protected RelationalDbClient buildDbClient(String serviceInstanceGuid) {
        def port = dbConfig.port ? dbConfig.port.toInteger() : 0
        return dbClientFactory.build(dbConfig.host, port, dbConfig.adminUser, dbConfig.adminPassword)
    }
}
