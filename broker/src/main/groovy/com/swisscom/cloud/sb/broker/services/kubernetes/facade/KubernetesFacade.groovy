package com.swisscom.cloud.sb.broker.services.kubernetes.facade

import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.client.rest.KubernetesClient
import com.swisscom.cloud.sb.broker.services.kubernetes.endpoint.parameters.EndpointMapperParamsDecorated
import com.swisscom.cloud.sb.broker.services.kubernetes.endpoint.parameters.KubernetesRedisConfigUrlParams
import com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis.config.KubernetesRedisConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplate
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplateManager
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.decorator.KubernetesTemplateVariablesDecorator
import com.swisscom.cloud.sb.broker.util.ServiceDetailKey
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.util.Pair
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component

@CompileStatic
interface KubernetesFacade {

    Collection<ServiceDetail> provision(ProvisionRequest context)

    void deprovision(DeprovisionRequest request)

}
