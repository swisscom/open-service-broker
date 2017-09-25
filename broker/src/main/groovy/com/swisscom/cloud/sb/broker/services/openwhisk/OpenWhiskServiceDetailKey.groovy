package com.swisscom.cloud.sb.broker.services.openwhisk

import com.swisscom.cloud.sb.broker.util.servicedetail.AbstractServiceDetailKey
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailType
import groovy.transform.CompileStatic

@CompileStatic
enum OpenWhiskServiceDetailKey implements AbstractServiceDetailKey{

    OPENWHISK_EXECUTION_URL("openwhisk_execution_url", ServiceDetailType.HOST),
    OPENWHISK_ADMIN_URL("openwhisk_admin_url", ServiceDetailType.HOST),
    OPENWHISK_UUID("openwhisk_uuid", ServiceDetailType.USERNAME),
    OPENWHISK_KEY("openwhisk_key", ServiceDetailType.PASSWORD),
    OPENWHISK_NAMESPACE("openwhisk_namespace", ServiceDetailType.OTHER),
    OPENWHISK_SUBJECT("openwhisk_subject", ServiceDetailType.OTHER)

    OpenWhiskServiceDetailKey(String key, ServiceDetailType serviceDetailType) {
        com_swisscom_cloud_sb_broker_util_servicedetail_AbstractServiceDetailKey__key = key
        com_swisscom_cloud_sb_broker_util_servicedetail_AbstractServiceDetailKey__serviceDetailType = serviceDetailType
    }
}
