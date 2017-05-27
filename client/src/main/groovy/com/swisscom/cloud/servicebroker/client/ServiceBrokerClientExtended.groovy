package com.swisscom.cloud.servicebroker.client

import com.swisscom.cloud.servicebroker.model.endpoint.Endpoint
import com.swisscom.cloud.servicebroker.model.usage.ServiceUsage
import groovy.transform.CompileStatic
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

@CompileStatic
class ServiceBrokerClientExtended extends ServiceBrokerClient implements IServiceBrokerClientExtended {
    private final String cfExtUsername
    private final String cfExtPassword

    ServiceBrokerClientExtended(RestTemplate restTemplate,String baseUrl, String cfUsername, String cfPassword,
        String cfExtUsername, String cfExtPassword) {
        super(restTemplate,baseUrl,cfUsername,cfPassword)
        this.cfExtUsername = cfExtUsername
        this.cfExtPassword = cfExtPassword
    }

    @Override
    ResponseEntity<List<Endpoint>> getEndpoint(String serviceInstanceId) {
        return restTemplate.exchange(appendPath('/custom/service_instances/{service_instance_id}/endpoint'),HttpMethod.GET,
                new HttpEntity(createSimpleAuthHeaders(cfExtUsername,cfExtPassword)),
                new ParameterizedTypeReference<List<Endpoint>>(){},serviceInstanceId)
    }

    @Override
    ResponseEntity<ServiceUsage> getUsage(String serviceInstanceId) {
        return restTemplate.exchange(appendPath('/custom/service_instances/{service_instance_id}/usage'),HttpMethod.GET,
                new HttpEntity(createSimpleAuthHeaders(cfExtUsername,cfExtPassword)),
                ServiceUsage.class,serviceInstanceId)
    }

    @Override
    ResponseEntity<Void> createOrUpdateServiceDefinition(String definition){
        return restTemplate.exchange(appendPath('/custom/admin/service-definition'),HttpMethod.POST,
                new HttpEntity(definition, createSimpleAuthHeaders(cfExtUsername,cfExtPassword)),Void.class)
    }
}