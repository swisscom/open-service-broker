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

package com.swisscom.cloud.sb.broker.services.mariadb

import com.google.common.annotations.VisibleForTesting
import com.google.common.base.Optional
import com.swisscom.cloud.sb.broker.backup.shield.ShieldBackupRestoreProvider
import com.swisscom.cloud.sb.broker.backup.shield.ShieldTarget
import com.swisscom.cloud.sb.broker.cfextensions.extensions.Extension
import com.swisscom.cloud.sb.broker.cfextensions.serviceusage.ServiceUsageProvider
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.CFServiceMetadata
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.UpdateRequest
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.relationaldb.RelationalDbClient
import com.swisscom.cloud.sb.broker.services.relationaldb.RelationalDbServiceProvider
import com.swisscom.cloud.sb.broker.updating.UpdateResponse
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailKey
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper
import com.swisscom.cloud.sb.model.usage.ServiceUsage
import com.swisscom.cloud.sb.model.usage.ServiceUsageType
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
class MariaDBServiceProvider extends RelationalDbServiceProvider implements ShieldBackupRestoreProvider, ServiceUsageProvider {
    private MariaDBConfig mariaDBConfig

    public static final String CLUSTER_METADATA_KEY = "clusterName"

    ServiceInstanceRepository serviceInstanceRepository

    @Autowired
    MariaDBServiceProvider(MariaDBConfig dbConfig, MariaDBClientFactory dbClientFactory, ServiceInstanceRepository serviceInstanceRepository) {
        super(dbConfig.default, dbClientFactory)
        this.mariaDBConfig = dbConfig
        this.serviceInstanceRepository = serviceInstanceRepository
    }

    @Override
    UpdateResponse update(UpdateRequest request) {
        ErrorCode.SERVICE_UPDATE_NOT_ALLOWED.throwNew()
        return null
    }

    @Override
    ServiceUsage findUsage(ServiceInstance serviceInstance, Optional<Date> endDate) {
        MariaDBClient client = buildDbClient(serviceInstance.guid) as MariaDBClient
        return new ServiceUsage(
                value: client.getUsageInBytes(ServiceDetailsHelper.from(serviceInstance.details).getValue(ServiceDetailKey.DATABASE)),
                type: ServiceUsageType.WATERMARK)
    }

    @Override
    ShieldTarget buildShieldTarget(ServiceInstance serviceInstance) {
        MariaDBConnectionConfig config = getConfiguration(serviceInstance)
        String database = ServiceDetailsHelper.from(serviceInstance.details).getValue(ServiceDetailKey.DATABASE)
        new MariaDBShieldTarget(user: config.adminUser,
                password: config.adminPassword,
                host: config.host,
                port: config.port,
                database: database)
    }

    @Override
    String shieldAgentUrl(ServiceInstance serviceInstance) {
        getConfiguration(serviceInstance).shieldAgentUrl
    }

    @Override
    protected RelationalDbClient buildDbClient(String serviceInstanceGuid) {
        MariaDBConnectionConfig mariaDBConnectionConfig = getConfiguration(serviceInstanceGuid)
        Integer port = mariaDBConnectionConfig.overwriteGaleraPortForShieldTesting ?
                mariaDBConnectionConfig.overwriteGaleraPortForShieldTesting.toInteger() :
                (mariaDBConnectionConfig.port ? mariaDBConnectionConfig.port.toInteger() : 0)
        return dbClientFactory.build(mariaDBConnectionConfig.host, port, mariaDBConnectionConfig.adminUser, mariaDBConnectionConfig.adminPassword)
    }

    @VisibleForTesting
    private MariaDBConnectionConfig getConfiguration(ServiceInstance serviceInstance) {
        CFServiceMetadata clusterNameMetadata = serviceInstance.plan.service.metadata.find { m -> m.key == CLUSTER_METADATA_KEY }
        clusterNameMetadata ? mariaDBConfig.getByName(clusterNameMetadata.value) : mariaDBConfig.default
    }

    @VisibleForTesting
    private MariaDBConnectionConfig getConfiguration(String serviceInstanceGuid) {
        getConfiguration(serviceInstanceRepository.findByGuid(serviceInstanceGuid))
    }

    Collection<Extension> buildExtensions() {
        return [new Extension(discovery_url: mariaDBConfig.default.discoveryURL)]
    }
}
