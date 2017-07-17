package com.swisscom.cloud.sb.broker.services.kubernetes.service.state

import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateMachineContext
import com.swisscom.cloud.sb.broker.services.kubernetes.facade.KubernetesFacade
import groovy.transform.CompileStatic

@CompileStatic
class KubernetesServiceProvisionStateMachineContext extends StateMachineContext {

    KubernetesFacade kubernetesFacade
}