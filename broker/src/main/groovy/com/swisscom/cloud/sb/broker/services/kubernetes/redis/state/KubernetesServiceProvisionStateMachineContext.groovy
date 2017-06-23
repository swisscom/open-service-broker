package com.swisscom.cloud.sb.broker.services.kubernetes.redis.state

import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateMachineContext
import com.swisscom.cloud.sb.broker.services.kubernetes.redis.KubernetesRedisClientRedisDecorated
import groovy.transform.CompileStatic

@CompileStatic
class KubernetesServiceProvisionStateMachineContext extends StateMachineContext {

    KubernetesRedisClientRedisDecorated kubernetesClientRedisDecorated
}