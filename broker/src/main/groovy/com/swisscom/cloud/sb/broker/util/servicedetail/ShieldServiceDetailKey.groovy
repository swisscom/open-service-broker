package com.swisscom.cloud.sb.broker.util.servicedetail

import groovy.transform.CompileStatic

@CompileStatic
enum ShieldServiceDetailKey implements AbstractServiceDetailKey{

    SHIELD_AGENT_PORT("shield_agent_port", ServiceDetailType.PORT),
    SHIELD_JOB_UUID("shield_job_uuid", ServiceDetailType.OTHER),
    SHIELD_TARGET_UUID("shield_target_uuid", ServiceDetailType.OTHER)

    ShieldServiceDetailKey(String key, ServiceDetailType serviceDetailType) {
        com_swisscom_cloud_sb_broker_util_servicedetail_AbstractServiceDetailKey__key = key
        com_swisscom_cloud_sb_broker_util_servicedetail_AbstractServiceDetailKey__serviceDetailType = serviceDetailType
    }
}
