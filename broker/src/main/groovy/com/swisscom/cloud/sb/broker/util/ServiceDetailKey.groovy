package com.swisscom.cloud.sb.broker.util

import groovy.transform.CompileStatic

@CompileStatic
enum ServiceDetailKey {

    HOST("host", ServiceDetailType.HOST),
    PORT("port", ServiceDetailType.PORT),
    ADMIN_USER("admin_user", ServiceDetailType.USERNAME),
    ADMIN_PASSWORD("admin_password", ServiceDetailType.PASSWORD),
    DATABASE("database", ServiceDetailType.OTHER),
    USER("user", ServiceDetailType.USERNAME),
    PASSWORD("password", ServiceDetailType.PASSWORD),
    SERVICE_MANAGER_ID("service_manager_id", ServiceDetailType.OTHER),
    UID("uid", ServiceDetailType.OTHER),
    ACCESS_HOST("access_host", ServiceDetailType.HOST),
    ACCESS_KEY("access_key", ServiceDetailType.OTHER),
    SHARED_SECRET("shared_secret", ServiceDetailType.OTHER),
    ADMIN_HOST("admin_host", ServiceDetailType.HOST),
    ADMIN_PORT("admin_port", ServiceDetailType.PORT),
    VHOST("vhost", ServiceDetailType.OTHER),
    KIBANA_HOST("kibana_host", ServiceDetailType.HOST),
    KIBANA_PORT("kibana_port", ServiceDetailType.PORT),
    LOGSTASH_HOST("logstash_host", ServiceDetailType.HOST),
    LOGSTASH_PORT("logstash_port", ServiceDetailType.PORT),
    ELASTIC_SEARCH_USER("elastic_search_user", ServiceDetailType.USERNAME),
    ELASTIC_SEARCH_PASSWORD("elastic_search_password", ServiceDetailType.PASSWORD),
    ELASTIC_SEARCH_HOST("elastic_search_host", ServiceDetailType.HOST),
    ELASTIC_SEARCH_PORT("elastic_search_port", ServiceDetailType.PORT),
    APIGEE_CLIENT_ID("apigee_client_id", ServiceDetailType.USERNAME),
    APIGEE_CLIENT_SECRET("apigee_client_secret", ServiceDetailType.PASSWORD),
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
    OAUTH2_CLIENT_ID("oauth2_client_id", ServiceDetailType.USERNAME),
    OAUTH2_CLIENT_SECRET("oauth2_client_secret", ServiceDetailType.PASSWORD),
    OAUTH2_CLIENT_GRANT_TYPES("oauth2_client_grant_types", ServiceDetailType.OTHER),
    OAUTH2_CLIENT_REDIRECT_URIS("oauth2_client_redirect_uris", ServiceDetailType.OTHER),
    OAUTH2_CLIENT_ACCESS_TOKEN_VALIDITY("oauth2_client_access_token_validity", ServiceDetailType.OTHER),
    RABBITMQ_HOSTNAMES("rabbitmq_hostnames", ServiceDetailType.HOST),
    RABBITMQ_USE_SSL("rabbitmq_use_ssl", ServiceDetailType.OTHER),
    REDIS_SENTINEL_PORT("redis_sentinel_port", ServiceDetailType.PORT),
    DELAY_IN_SECONDS("delay_in_seconds", ServiceDetailType.OTHER),
    REDIS_CONFIG_COMMAND("redis_config_command", ServiceDetailType.OTHER),
    REDIS_SLAVEOF_COMMAND("redis_slaveof_command", ServiceDetailType.OTHER),
    CLOUD_PROVIDER_SERVER_GROUP_ID("cloud_provider_server_group_id", ServiceDetailType.OTHER),
    BOSH_TASK_ID_FOR_DEPLOY("bosh_task_id_for_deploy", ServiceDetailType.OTHER),
    BOSH_TASK_ID_FOR_UNDEPLOY("bosh_task_id_for_undeploy", ServiceDetailType.OTHER),
    BOSH_DEPLOYMENT_ID("bosh_deployment_id", ServiceDetailType.OTHER),
    MONGODB_ENTERPRISE_HEALTH_CHECK_USER("mongodb_enterprise_health_check_user", ServiceDetailType.OTHER),
    MONGODB_ENTERPRISE_HEALTH_CHECK_PASSWORD("mongodb_enterprise_health_check_password", ServiceDetailType.OTHER),
    ECS_NAMESPACE_NAME("ecs_namespace_name", ServiceDetailType.OTHER),
    ECS_NAMESPACE_USER("ecs_namespace_user", ServiceDetailType.OTHER),
    ECS_NAMESPACE_SECRET("ecs_namespace_secret", ServiceDetailType.OTHER),
    KUBERNETES_REDIS_HOST("kubernetes_redis_service_host", ServiceDetailType.HOST),
    KUBERNETES_REDIS_PASSWORD("kubernetes_redis_service_password", ServiceDetailType.PASSWORD),
    KUBERNETES_REDIS_PORT_MASTER("kubernetes_redis_service_port_master", ServiceDetailType.PORT),
    KUBERNETES_REDIS_PORT_SLAVE("kubernetes_redis_service_port_slave", ServiceDetailType.PORT),
    OPENWHISK_EXECUTION_URL("openwhisk_execution_url", ServiceDetailType.HOST),
    OPENWHISK_ADMIN_URL("openwhisk_admin_url", ServiceDetailType.HOST),
    OPENWHISK_UUID("openwhisk_uuid", ServiceDetailType.USERNAME),
    OPENWHISK_KEY("openwhisk_key", ServiceDetailType.PASSWORD),
    OPENWHISK_NAMESPACE("openwhisk_namespace", ServiceDetailType.OTHER),
    OPENWHISK_SUBJECT("openwhisk_subject", ServiceDetailType.OTHER),
    SHIELD_AGENT_PORT("shield_agent_port", ServiceDetailType.PORT),
    SHIELD_JOB_UUID("shield_job_uuid", ServiceDetailType.OTHER),
    SHIELD_TARGET_UUID("shield_target_uuid", ServiceDetailType.OTHER)

    private final String key
    private final ServiceDetailType serviceDetailType

    ServiceDetailKey(String key, ServiceDetailType serviceDetailType) {
        this.key = key
        this.serviceDetailType = serviceDetailType
    }

    ServiceDetailType detailType() {
        return serviceDetailType
    }

    String getKey() {
        return key
    }

}
