package com.swisscom.cf.servicebroker.client

import com.swisscom.cf.servicebroker.client.model.DeleteServiceInstanceBindingRequest
import com.swisscom.cf.servicebroker.client.model.DeleteServiceInstanceRequest
import com.swisscom.cf.servicebroker.client.model.LastOperationResponse
import groovy.transform.CompileStatic
import org.apache.commons.codec.binary.Base64
import org.springframework.cloud.servicebroker.model.*
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

import java.nio.charset.Charset

@CompileStatic
class ServiceBrokerClient implements IServiceBrokerClient {
    protected final RestTemplate restTemplate
    protected final String baseUrl
    private final String username
    private final String password

    ServiceBrokerClient(RestTemplate restTemplate, String baseUrl, String username, String password) {
        this.restTemplate = restTemplate
        this.baseUrl = baseUrl
        this.username = username
        this.password = password
    }

    ServiceBrokerClient(String baseUrl, String username, String password) {
        this(new RestTemplate(), baseUrl, username, password)
    }

    @Override
    ResponseEntity<Catalog> getCatalog() {
        return restTemplate.exchange(appendPath('/v2/catalog'), HttpMethod.GET, new HttpEntity(createSimpleAuthHeaders(username, password)), Catalog.class)
    }

    @Override
    ResponseEntity<LastOperationResponse> getServiceInstanceLastOperation(String serviceInstanceId) {
        return restTemplate.exchange(appendPath("/v2/service_instances/{serviceInstanceId}/last_operation"),
                HttpMethod.GET, new HttpEntity(createSimpleAuthHeaders(username, password)), LastOperationResponse.class,
                serviceInstanceId)
    }

    @Override
    ResponseEntity<CreateServiceInstanceResponse> createServiceInstance(CreateServiceInstanceRequest request) {
        return restTemplate.exchange(appendPath("/v2/service_instances/{serviceInstanceId}?accepts_incomplete={asyncAccepted}"),
                HttpMethod.PUT, new HttpEntity<CreateServiceInstanceRequest>(request, createSimpleAuthHeaders(username, password)),
                CreateServiceInstanceResponse.class, request.serviceInstanceId, request.asyncAccepted)
    }

    @Override
    ResponseEntity<UpdateServiceInstanceResponse> updateServiceInstance(UpdateServiceInstanceRequest request) {
        return restTemplate.exchange(appendPath("/v2/service_instances/{serviceInstanceId}?accepts_incomplete={asyncAccepted}"),
                HttpMethod.PATCH, new HttpEntity<UpdateServiceInstanceRequest>(request, createSimpleAuthHeaders(username, password)),
                UpdateServiceInstanceResponse.class, request.serviceInstanceId, request.asyncAccepted)
    }

    @Override
    ResponseEntity<Void> deleteServiceInstance(DeleteServiceInstanceRequest request) {
        return restTemplate.exchange(appendPath("/v2/service_instances/{serviceInstanceId}?service_id={serviceId}&plan_id={planId}&accepts_incomplete={asyncAccepted}"),
                HttpMethod.DELETE, new HttpEntity<?>(createSimpleAuthHeaders(username, password)),
                Void.class, request.serviceInstanceId, request.serviceId, request.planId, request.asyncAccepted)
    }

    @Override
    ResponseEntity<CreateServiceInstanceAppBindingResponse> createServiceInstanceBinding(CreateServiceInstanceBindingRequest request) {
        return restTemplate.exchange(appendPath("/v2/service_instances/{serviceInstanceId}/service_bindings/{bindingId}"),
                HttpMethod.PUT, new HttpEntity<CreateServiceInstanceBindingRequest>(request, createSimpleAuthHeaders(username, password)),
                CreateServiceInstanceAppBindingResponse.class, request.serviceInstanceId, request.bindingId)
    }

    @Override
    ResponseEntity<Void> deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request) {
        return restTemplate.exchange(appendPath("/v2/service_instances/{serviceInstanceId}/service_bindings/{bindingId}?service_id={serviceId}&plan_id={planId}"),
                HttpMethod.DELETE, new HttpEntity<DeleteServiceInstanceBindingRequest>(request, createSimpleAuthHeaders(username, password)),
                Void.class, request.serviceInstanceId, request.bindingId, request.serviceId, request.planId)
    }

    private static HttpHeaders createSimpleAuthHeaders(String username, String password) {
        def result = new HttpHeaders()
        if (password) {
            String auth = username + ":" + password
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")))
            String authHeader = "Basic " + new String(encodedAuth)
            result.set("Authorization", authHeader)
        }
        return result
    }

    protected String appendPath(String path) {
        return baseUrl + path
    }
}
