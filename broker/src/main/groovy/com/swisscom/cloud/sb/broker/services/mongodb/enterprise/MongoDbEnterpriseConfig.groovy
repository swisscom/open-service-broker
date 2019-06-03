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

package com.swisscom.cloud.sb.broker.services.mongodb.enterprise

import com.swisscom.cloud.sb.broker.services.AsyncServiceConfigImpl
import com.swisscom.cloud.sb.broker.services.bosh.BoshBasedServiceConfig
import com.swisscom.cloud.sb.broker.services.bosh.resources.GenericConfig
import com.swisscom.cloud.sb.broker.services.common.TemplateConfig
import groovy.transform.CompileStatic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@CompileStatic
@Component
@ConfigurationProperties(prefix = "com.swisscom.cloud.sb.broker.service.mongodbent")
class MongoDbEnterpriseConfig extends AsyncServiceConfigImpl implements BoshBasedServiceConfig {
    String boshDirectorBaseUrl
    String boshDirectorUsername
    String boshDirectorPassword
    String portRange
    String boshManifestFolder
    boolean shuffleAzs
    List<GenericConfig> genericConfigs
    String opsManagerUrl
    String opsManagerUrlForAutomationAgent
    String opsManagerUser
    String opsManagerApiKey
    String opsManagerUserRoles
    String opsManagerIpWhiteList
    String dbFolder
    String libFolder
    String logFolder
    int authSchemaVersion
    String mongoDbVersion
    String featureCompatibilityVersion
    boolean configureDefaultBackupOptions
    int snapshotIntervalHours
    int snapshotRetentionDays
    int dailySnapshotRetentionDays
    int weeklySnapshotRetentionWeeks
    int monthlySnapshotRetentionMonths
    int pointInTimeWindowHours
    int backupConfigRetryInMilliseconds = 5000
    boolean advancedBinding
    TemplateConfig templateConfig

    @Override
    public String toString() {
        return "MongoDbEnterpriseConfig{" +
               "com_swisscom_cloud_sb_broker_services_bosh_BoshBasedServiceConfig__retryIntervalInSeconds=" + this.retryIntervalInSeconds +
               ", com_swisscom_cloud_sb_broker_services_bosh_BoshBasedServiceConfig__maxRetryDurationInMinutes=" + this.maxRetryDurationInMinutes +
               ", com_swisscom_cloud_sb_broker_services_bosh_BoshBasedServiceConfig__portRange='" + this.portRange + '\'' +
               ", com_swisscom_cloud_sb_broker_services_bosh_BoshBasedServiceConfig__boshManifestFolder='" + this.boshManifestFolder + '\'' +
               ", com_swisscom_cloud_sb_broker_services_bosh_BoshConfig__boshDirectorBaseUrl='" + this.boshDirectorBaseUrl + '\'' +
               ", com_swisscom_cloud_sb_broker_services_bosh_BoshConfig__boshDirectorUsername='" + this.boshDirectorUsername + '\'' +
               ", com_swisscom_cloud_sb_broker_services_common_endpoint_EndpointConfig__ipRanges='" + this.ipRanges + '\'' +
               ", com_swisscom_cloud_sb_broker_services_common_endpoint_EndpointConfig__protocols='" + this.protocols + '\'' +
               ", opsManagerUrl='" + opsManagerUrl + '\'' +
               ", opsManagerUrlForAutomationAgent='" + opsManagerUrlForAutomationAgent + '\'' +
               ", opsManagerUser='" + opsManagerUser + '\'' +
               ", opsManagerUserRoles='" + opsManagerUserRoles + '\'' +
               ", opsManagerIpWhiteList='" + opsManagerIpWhiteList + '\'' +
               ", portRange='" + portRange + '\'' +
               ", dbFolder='" + dbFolder + '\'' +
               ", libFolder='" + libFolder + '\'' +
               ", logFolder='" + logFolder + '\'' +
               ", authSchemaVersion=" + authSchemaVersion +
               ", mongoDbVersion='" + mongoDbVersion + '\'' +
               ", featureCompatibilityVersion='" + featureCompatibilityVersion + '\'' +
               ", configureDefaultBackupOptions=" + configureDefaultBackupOptions +
               ", snapshotIntervalHours=" + snapshotIntervalHours +
               ", snapshotRetentionDays=" + snapshotRetentionDays +
               ", dailySnapshotRetentionDays=" + dailySnapshotRetentionDays +
               ", weeklySnapshotRetentionWeeks=" + weeklySnapshotRetentionWeeks +
               ", monthlySnapshotRetentionMonths=" + monthlySnapshotRetentionMonths +
               ", pointInTimeWindowHours=" + pointInTimeWindowHours +
               ", retryIntervalInSeconds=" + retryIntervalInSeconds +
               ", maxRetryDurationInMinutes=" + maxRetryDurationInMinutes +
               ", backupConfigRetryInMilliseconds=" + backupConfigRetryInMilliseconds +
               ", advancedBinding=" + advancedBinding +
               '}'
    }
}
