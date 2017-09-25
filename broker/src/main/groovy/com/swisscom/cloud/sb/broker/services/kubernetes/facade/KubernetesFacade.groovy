package com.swisscom.cloud.sb.broker.services.kubernetes.facade

import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import groovy.transform.CompileStatic

@CompileStatic
trait KubernetesFacade {

    abstract Collection<ServiceDetail> provision(ProvisionRequest context)

    abstract void deprovision(DeprovisionRequest request)

    abstract boolean isKubernetesDeploymentSuccessful(String serviceInstanceGuid)

    abstract boolean isKubernetesNamespaceDeleted(String serviceInstanceGuid)

}
