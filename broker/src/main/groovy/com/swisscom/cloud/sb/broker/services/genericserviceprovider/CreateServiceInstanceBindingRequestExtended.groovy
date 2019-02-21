package com.swisscom.cloud.sb.broker.services.genericserviceprovider

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest

class CreateServiceInstanceBindingRequestExtended extends CreateServiceInstanceBindingRequest{

    @JsonSerialize
    @JsonProperty("proxy_service")
    ProxyService proxyService

    CreateServiceInstanceBindingRequestExtended(String serviceDefinitionId, String planId, String appGuid,
                                                Map<String, Object> bindResource, Map<String, Object> parameters,
                                                ProxyService proxyService) {
        super(serviceDefinitionId, planId, appGuid, bindResource, parameters)

        this.proxyService = proxyService
    }
}
