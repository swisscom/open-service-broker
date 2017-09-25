package com.swisscom.cloud.sb.broker.services.bosh

import com.swisscom.cloud.sb.broker.util.servicedetail.AbstractServiceDetailKey
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailType
import groovy.transform.CompileStatic

@CompileStatic
enum BoshServiceDetailKey implements AbstractServiceDetailKey{

    CLOUD_PROVIDER_SERVER_GROUP_ID("cloud_provider_server_group_id", ServiceDetailType.OTHER),
    BOSH_TASK_ID_FOR_DEPLOY("bosh_task_id_for_deploy", ServiceDetailType.OTHER),
    BOSH_TASK_ID_FOR_UNDEPLOY("bosh_task_id_for_undeploy", ServiceDetailType.OTHER),
    BOSH_DEPLOYMENT_ID("bosh_deployment_id", ServiceDetailType.OTHER)

    BoshServiceDetailKey(String key, ServiceDetailType serviceDetailType) {
        com_swisscom_cloud_sb_broker_util_servicedetail_AbstractServiceDetailKey__key = key
        com_swisscom_cloud_sb_broker_util_servicedetail_AbstractServiceDetailKey__serviceDetailType = serviceDetailType
    }
}
