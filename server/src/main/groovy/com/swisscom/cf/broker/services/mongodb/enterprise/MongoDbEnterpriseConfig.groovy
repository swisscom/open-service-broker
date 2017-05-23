package com.swisscom.cf.broker.services.mongodb.enterprise

import com.swisscom.cf.broker.services.bosh.BoshBasedServiceConfig
import groovy.transform.CompileStatic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@CompileStatic
@Configuration
@ConfigurationProperties(prefix = "com.swisscom.cf.broker.service.mongodbent")
class MongoDbEnterpriseConfig implements BoshBasedServiceConfig {
    String opsManagerUrl
    String opsManagerUrlForAutomationAgent
    String opsManagerUser
    String opsManagerApiKey
    String opsManagerUserRoles
    String opsManagerIpWhiteList
    String portRange
    String dbFolder
    String libFolder
    String logFolder
    int authSchemaVersion
    String mongoDbVersion
    boolean configureDefaultBackupOptions
    int snapshotIntervalHours
    int snapshotRetentionDays
    int dailySnapshotRetentionDays
    int weeklySnapshotRetentionWeeks
    int monthlySnapshotRetentionMonths
    int pointInTimeWindowHours
    int retryIntervalInSeconds
    int maxRetryDurationInMinutes
    int backupConfigRetryInMilliseconds = 5000
    boolean advancedBinding


    @Override
    public String toString() {
        return "MongoDbEnterpriseConfig{" +
                "com_swisscom_cf_broker_services_bosh_BoshBasedServiceConfig__retryIntervalInSeconds=" + com_swisscom_cf_broker_services_bosh_BoshBasedServiceConfig__retryIntervalInSeconds +
                ", com_swisscom_cf_broker_services_bosh_BoshBasedServiceConfig__maxRetryDurationInMinutes=" + com_swisscom_cf_broker_services_bosh_BoshBasedServiceConfig__maxRetryDurationInMinutes +
                ", com_swisscom_cf_broker_services_bosh_BoshBasedServiceConfig__portRange='" + com_swisscom_cf_broker_services_bosh_BoshBasedServiceConfig__portRange + '\'' +
                ", com_swisscom_cf_broker_services_bosh_BoshBasedServiceConfig__openstackkUrl='" + com_swisscom_cf_broker_services_bosh_BoshBasedServiceConfig__openstackkUrl + '\'' +
                ", com_swisscom_cf_broker_services_bosh_BoshBasedServiceConfig__openstackUsername='" + com_swisscom_cf_broker_services_bosh_BoshBasedServiceConfig__openstackUsername + '\'' +
                ", com_swisscom_cf_broker_services_bosh_BoshBasedServiceConfig__openstackTenantName='" + com_swisscom_cf_broker_services_bosh_BoshBasedServiceConfig__openstackTenantName + '\'' +
                ", com_swisscom_cf_broker_services_bosh_BoshBasedServiceConfig__boshManifestFolder='" + com_swisscom_cf_broker_services_bosh_BoshBasedServiceConfig__boshManifestFolder + '\'' +
                ", com_swisscom_cf_broker_services_bosh_BoshConfig__boshDirectorBaseUrl='" + com_swisscom_cf_broker_services_bosh_BoshConfig__boshDirectorBaseUrl + '\'' +
                ", com_swisscom_cf_broker_services_bosh_BoshConfig__boshDirectorUsername='" + com_swisscom_cf_broker_services_bosh_BoshConfig__boshDirectorUsername + '\'' +
                ", com_swisscom_cf_broker_services_common_endpoint_EndpointConfig__ipRange='" + com_swisscom_cf_broker_cfextensions_endpoint_EndpointConfig__ipRange + '\'' +
                ", com_swisscom_cf_broker_services_common_endpoint_EndpointConfig__protocols='" + com_swisscom_cf_broker_cfextensions_endpoint_EndpointConfig__protocols + '\'' +
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
                '}';
    }
}
