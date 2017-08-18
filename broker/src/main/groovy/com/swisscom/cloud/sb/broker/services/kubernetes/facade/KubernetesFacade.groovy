package com.swisscom.cloud.sb.broker.services.kubernetes.facade

import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import groovy.transform.CompileStatic

@CompileStatic
interface KubernetesFacade {

    Collection<ServiceDetail> provision(ProvisionRequest context)

    void deprovision(DeprovisionRequest request)

    boolean isKubernetesDeploymentSuccessful(String serviceInstanceGuid)

}
