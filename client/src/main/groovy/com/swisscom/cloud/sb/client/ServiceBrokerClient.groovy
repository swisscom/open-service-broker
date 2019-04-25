/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.client

import com.swisscom.cloud.sb.client.model.LastOperationResponse
import com.swisscom.cloud.sb.client.model.ServiceInstanceBindingResponse
import com.swisscom.cloud.sb.client.model.ServiceInstanceResponse
import com.swisscom.cloud.sb.client.model.ProvisionResponseDto
import com.swisscom.cloud.sb.client.model.CreateServiceInstanceResponse
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import org.apache.commons.codec.binary.Base64
import org.springframework.cloud.servicebroker.model.catalog.Catalog
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingRequest
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.instance.UpdateServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.instance.UpdateServiceInstanceResponse
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
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
        this(new RestTemplate(new HttpComponentsClientHttpRequestFactory()), baseUrl, username, password)
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    def <T> HttpEntity<T>createHttpEntity(T request) {
        return new HttpEntity<T>(request, createSimpleAuthHeaders(username, password))
    }

    HttpEntity createHttpEntity() {
        return new HttpEntity(createSimpleAuthHeaders(username, password))
    }

    @Override
    def <T> ResponseEntity<T> exchange(
            String relativePath,
            HttpMethod method,
            ParameterizedTypeReference<T> responseType,
            Object... uriVariables) {
        return exchange(relativePath, method, createHttpEntity(), responseType, uriVariables)
    }

    @Override
    def <T> ResponseEntity<T> exchange(
            String relativePath,
            HttpMethod method,
            HttpEntity<?> requestEntity,
            ParameterizedTypeReference<T> responseType,
            Object... uriVariables) {
        return restTemplate.exchange(appendPath(relativePath), method, requestEntity, responseType, uriVariables)
    }

    @Override
    def <T> ResponseEntity<T> exchange(
            String relativePath,
            HttpMethod method,
            Class<T> responseType,
            Object... uriVariables) {
        return exchange(relativePath, method, createHttpEntity(), responseType, uriVariables)
    }

    @Override
    def <T> ResponseEntity<T> exchange(
            String relativePath,
            HttpMethod method,
            HttpEntity<?> requestEntity,
            Class<T> responseType,
            Object... uriVariables) {
        return restTemplate.exchange(appendPath(relativePath), method, requestEntity, responseType, uriVariables)
    }

    @Override
    ResponseEntity<Catalog> getCatalog() {
        return exchange('/v2/catalog', HttpMethod.GET, Catalog.class)
    }

    @Override
    ResponseEntity<LastOperationResponse> getServiceInstanceLastOperation(String serviceInstanceId) {
        return exchange("/v2/service_instances/{serviceInstanceId}/last_operation?service_id={serviceId}&plan_id={planId}",
                HttpMethod.GET, LastOperationResponse.class,
                serviceInstanceId, "serviceId", "planId")
    }

    @Override
    ResponseEntity<LastOperationResponse> getServiceInstanceLastOperation(String serviceInstanceId, String operationId) {
        return exchange("/v2/service_instances/{serviceInstanceId}/last_operation?operation={operationId}&service_id={serviceId}&plan_id={planId}",
                HttpMethod.GET, LastOperationResponse.class,
                serviceInstanceId, operationId, "serviceId", "planId")
    }

    @Override
    ResponseEntity<CreateServiceInstanceResponse> createServiceInstance(CreateServiceInstanceRequest request) {
        return exchange("/v2/service_instances/{serviceInstanceId}?accepts_incomplete={asyncAccepted}",
                HttpMethod.PUT, createHttpEntity(request),
                CreateServiceInstanceResponse.class, request.serviceInstanceId, request.asyncAccepted)
    }

    @Override
    ResponseEntity<ProvisionResponseDto> provision(CreateServiceInstanceRequest request) {
        return exchange("/v2/service_instances/{serviceInstanceId}?accepts_incomplete={asyncAccepted}",
                HttpMethod.PUT, createHttpEntity(request),
                ProvisionResponseDto.class, request.serviceInstanceId, request.asyncAccepted)
    }

    @Override
    ResponseEntity<UpdateServiceInstanceResponse> updateServiceInstance(UpdateServiceInstanceRequest request) {
        return exchange("/v2/service_instances/{serviceInstanceId}?accepts_incomplete={asyncAccepted}",
                HttpMethod.PATCH, createHttpEntity(request),
                UpdateServiceInstanceResponse.class, request.serviceInstanceId, request.asyncAccepted)
    }

    @Override
    ResponseEntity<Void> deleteServiceInstance(DeleteServiceInstanceRequest request) {
        return exchange("/v2/service_instances/{serviceInstanceId}?service_id={serviceId}&plan_id={planId}&accepts_incomplete={asyncAccepted}",
                HttpMethod.DELETE,
                Void.class, request.serviceInstanceId, request.serviceInstanceId, request.planId, request.asyncAccepted)
    }

    @Override
    ResponseEntity<CreateServiceInstanceAppBindingResponse> createServiceInstanceBinding(CreateServiceInstanceBindingRequest request) {
        return exchange("/v2/service_instances/{serviceInstanceId}/service_bindings/{bindingId}",
                HttpMethod.PUT, createHttpEntity(request),
                CreateServiceInstanceAppBindingResponse.class, request.serviceInstanceId, request.bindingId)
    }

    @Override
    ResponseEntity<Void> deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request) {
        return exchange("/v2/service_instances/{serviceInstanceId}/service_bindings/{bindingId}?service_id={serviceId}&plan_id={planId}",
                HttpMethod.DELETE, createHttpEntity(request),
                Void.class, request.serviceInstanceId, request.bindingId, request.serviceDefinitionId, request.planId)
    }

    @Override
    ResponseEntity<ServiceInstanceResponse> getServiceInstance(String serviceInstanceId) {
        return exchange("/v2/service_instances/{serviceInstanceId}", HttpMethod.GET, ServiceInstanceResponse.class, serviceInstanceId)
    }

    @Override
    ResponseEntity<ServiceInstanceBindingResponse> getServiceInstanceBinding(String serviceInstanceId, String bindingId) {
        return exchange("/v2/service_instances/{instanceId}/service_bindings/{bindingId}",
                HttpMethod.GET, ServiceInstanceBindingResponse.class, serviceInstanceId, bindingId)
    }

    static protected HttpHeaders createSimpleAuthHeaders(String username, String password) {
        def result = new HttpHeaders()
        if (password) {
            String auth = username + ":" + password
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")))
            String authHeader = "Basic " + new String(encodedAuth)
            result.set("Authorization", authHeader)
        }
        return result
    }

    static protected HttpHeaders addJsonContentTypeHeader(HttpHeaders headers) {
        headers.add("Content-Type", "application/json")

        return headers
    }

    protected String appendPath(String path) {
        return baseUrl + path
    }
}
