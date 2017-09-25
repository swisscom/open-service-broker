package com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis

import com.swisscom.cloud.sb.broker.util.servicedetail.AbstractServiceDetailKey
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailType
import groovy.transform.CompileStatic

@CompileStatic
enum KubernetesRedisServiceDetailKey implements AbstractServiceDetailKey{

    KUBERNETES_REDIS_HOST("kubernetes_redis_service_host", ServiceDetailType.HOST),
    KUBERNETES_REDIS_PASSWORD("kubernetes_redis_service_password", ServiceDetailType.PASSWORD),
    KUBERNETES_REDIS_PORT_MASTER("kubernetes_redis_service_port_master", ServiceDetailType.PORT),
    KUBERNETES_REDIS_PORT_SLAVE("kubernetes_redis_service_port_slave", ServiceDetailType.PORT)

    KubernetesRedisServiceDetailKey(String key, ServiceDetailType serviceDetailType) {
        com_swisscom_cloud_sb_broker_util_servicedetail_AbstractServiceDetailKey__key = key
        com_swisscom_cloud_sb_broker_util_servicedetail_AbstractServiceDetailKey__serviceDetailType = serviceDetailType
    }
}
