package com.swisscom.cloud.sb.broker.services.mongodb.enterprise

import com.swisscom.cloud.sb.broker.util.servicedetail.AbstractServiceDetailKey
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailType
import groovy.transform.CompileStatic

@CompileStatic
enum MongoDbEnterpriseServiceDetailKey implements AbstractServiceDetailKey{

    MONGODB_ENTERPRISE_GROUP_ID("mongodb_enterprise_group_id", ServiceDetailType.OTHER),
    MONGODB_ENTERPRISE_GROUP_NAME("mongodb_enterprise_group_name", ServiceDetailType.OTHER),
    MONGODB_ENTERPRISE_AGENT_API_KEY("mongodb_enterprise_agent_api_key", ServiceDetailType.OTHER),
    MONGODB_ENTERPRISE_OPS_MANAGER_USER_NAME("mongodb_enterprise_ops_manager_user_name", ServiceDetailType.OTHER),
    MONGODB_ENTERPRISE_OPS_MANAGER_USER_ID("mongodb_enterprise_ops_manager_user_id", ServiceDetailType.OTHER),
    MONGODB_ENTERPRISE_OPS_MANAGER_PASSWORD("mongodb_enterprise_ops_manager_password", ServiceDetailType.OTHER),
    MONGODB_ENTERPRISE_TARGET_AUTOMATION_GOAL_VERSION("mongodb_enterprise_target_automation_goal_version", ServiceDetailType.OTHER),
    MONGODB_ENTERPRISE_TARGET_AGENT_COUNT("mongodb_enterprise_target_agent_count", ServiceDetailType.OTHER),
    MONGODB_ENTERPRISE_REPLICA_SET("mongodb_enterprise_replica_set", ServiceDetailType.OTHER),
    MONGODB_ENTERPRISE_BACKUP_AGENT_USER("mongodb_enterprise_backup_agent_user", ServiceDetailType.OTHER),
    MONGODB_ENTERPRISE_BACKUP_AGENT_PASSWORD("mongodb_enterprise_backup_agent_password", ServiceDetailType.OTHER),
    MONGODB_ENTERPRISE_MONITORING_AGENT_USER("mongodb_enterprise_monitoring_agent_user", ServiceDetailType.OTHER),
    MONGODB_ENTERPRISE_MONITORING_AGENT_PASSWORD("mongodb_enterprise_monitoring_agent_password", ServiceDetailType.OTHER),
    MONGODB_ENTERPRISE_HEALTH_CHECK_USER("mongodb_enterprise_health_check_user", ServiceDetailType.OTHER),
    MONGODB_ENTERPRISE_HEALTH_CHECK_PASSWORD("mongodb_enterprise_health_check_password", ServiceDetailType.OTHER)

    MongoDbEnterpriseServiceDetailKey(String key, ServiceDetailType serviceDetailType) {
        com_swisscom_cloud_sb_broker_util_servicedetail_AbstractServiceDetailKey__key = key
        com_swisscom_cloud_sb_broker_util_servicedetail_AbstractServiceDetailKey__serviceDetailType = serviceDetailType
    }
}
