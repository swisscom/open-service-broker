package com.swisscom.cloud.sb.broker.services.lapi

import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.BindResponse
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.provisioning.ProvisionResponse
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
@CompileStatic
@Slf4j
class LapiServiceProvider implements ServiceProvider {

    private RestTemplateBuilder restTemplateBuilder

    LapiConfig lapiConfig

    @Autowired
    LapiServiceProvider(RestTemplateBuilder restTemplateBuilder, LapiConfig lapiConfig) {
        this.lapiConfig = lapiConfig
        this.restTemplateBuilder = restTemplateBuilder
        this.restTemplateBuilder = restTemplateBuilder.withBasicAuthentication(lapiConfig.lapiUsername, lapiConfig.lapiPassword)
    }

    @Override
    ProvisionResponse provision(ProvisionRequest request) {
        log.info("Hey lets provision")

        RestTemplate restTemplate = restTemplateBuilder.build()
        String url = "http://0.0.0.0:4567/v2/service_instances/${request.serviceInstanceGuid}"
        restTemplate.put(url, request, ProvisionResponse.class)
        return new ProvisionResponse(isAsync: false)
    }

    @Override
    DeprovisionResponse deprovision(DeprovisionRequest request) {
        log.info("lets deprovision!")

        RestTemplate restTemplate = restTemplateBuilder.build()
        String url = "http://0.0.0.0:4567/v2/service_instances/${request.serviceInstanceGuid}"
        restTemplate.delete(url)
        return new DeprovisionResponse(isAsync: false)
    }

    @Override
    //putting and reading bindingID from request.parameters ok?
    BindResponse bind(BindRequest request) {
        log.info("lets bind!")
        String url = "http://0.0.0.0:4567/v2/service_instances/${request.serviceInstance.guid}/service-bindings/${request.binding_guid}"
        RestTemplate restTemplate = restTemplateBuilder.build()
        restTemplate.put(url, request, BindResponse.class)
        return new BindResponse()
    }

    @Override
    void unbind(UnbindRequest request) {
        log.info("lets unbind")
        String url = "http://0.0.0.0:4567/v2/service_instances/${request.serviceInstance.guid}/service-bindings/serviceBindingId"
        RestTemplate restTemplate = restTemplateBuilder.build()
        restTemplate.delete(url)
    }

    @Override
    UpdateResponse update(UpdateRequest request) {
        return null
    }
}
