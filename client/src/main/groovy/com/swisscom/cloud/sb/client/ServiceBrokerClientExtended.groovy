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

import com.swisscom.cloud.sb.model.backup.BackupDto
import com.swisscom.cloud.sb.model.backup.RestoreDto
import com.swisscom.cloud.sb.model.endpoint.Endpoint
import com.swisscom.cloud.sb.model.health.ServiceHealth
import com.swisscom.cloud.sb.model.usage.ServiceUsage
import com.swisscom.cloud.sb.model.usage.extended.ServiceUsageItem
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

@CompileStatic
class ServiceBrokerClientExtended extends ServiceBrokerClient implements IServiceBrokerClientExtended {
    private final String cfExtUsername
    private final String cfExtPassword

    private final String cfUsername
    private final String cfPassword

    ServiceBrokerClientExtended(RestTemplate restTemplate,String baseUrl, String cfUsername, String cfPassword,
        String cfExtUsername, String cfExtPassword) {
        super(restTemplate,baseUrl,cfUsername,cfPassword)

        this.cfExtUsername = cfExtUsername
        this.cfExtPassword = cfExtPassword

        this.cfUsername = cfUsername
        this.cfPassword = cfPassword
    }

    def <T> HttpEntity<T>createExtendedHttpEntity(T request) {
        return new HttpEntity<T>(request, addJsonContentTypeHeader(createSimpleAuthHeaders(cfExtUsername, cfExtPassword)))
    }

    def HttpEntity createExtendedHttpEntity() {
        return new HttpEntity(addJsonContentTypeHeader(createSimpleAuthHeaders(cfExtUsername, cfExtPassword)))
    }

    @Override
    def <T> ResponseEntity<T> extendedExchange(
            String relativePath,
            HttpMethod method,
            ParameterizedTypeReference<T> responseType,
            Object... uriVariables) {
        return extendedExchange(relativePath, method, createExtendedHttpEntity(), responseType, uriVariables)
    }

    @Override
    def <T> ResponseEntity<T> extendedExchange(
            String relativePath,
            HttpMethod method,
            HttpEntity<?> requestEntity,
            ParameterizedTypeReference<T> responseType,
            Object... uriVariables) {
        return restTemplate.exchange(appendPath(relativePath), method, requestEntity, responseType, uriVariables)
    }

    @Override
    def <T> ResponseEntity<T> extendedExchange(
            String relativePath,
            HttpMethod method,
            Class<T> responseType,
            Object... uriVariables) {
        return extendedExchange(relativePath, method, createExtendedHttpEntity(), responseType, uriVariables)
    }

    @Override
    def <T> ResponseEntity<T> extendedExchange(
            String relativePath,
            HttpMethod method,
            HttpEntity<?> requestEntity,
            Class<T> responseType,
            Object... uriVariables) {
        return restTemplate.exchange(appendPath(relativePath), method, requestEntity, responseType, uriVariables)
    }

    @Override
    ResponseEntity<List<Endpoint>> getEndpoint(String serviceInstanceId) {
        return extendedExchange('/custom/service_instances/{service_instance_id}/endpoint', HttpMethod.GET,
                new ParameterizedTypeReference<List<Endpoint>>(){}, serviceInstanceId)
    }

    @Override
    ResponseEntity<ServiceUsage> getUsage(String serviceInstanceId) {
        return extendedExchange('/custom/service_instances/{service_instance_id}/usage',HttpMethod.GET,
                ServiceUsage.class,serviceInstanceId)
    }

    @Override
    @TypeChecked(TypeCheckingMode.SKIP)
    ResponseEntity<Set<ServiceUsageItem>> getExtendedUsage(String serviceInstanceId) {
        return exchange('/v2/service_instances/{service_instance_id}/usage',HttpMethod.GET,
                Set.class,serviceInstanceId)
    }

    @Override
    ResponseEntity<ServiceHealth> getHealth(String serviceInstanceId) {
        return exchange('/v2/service_instances/{service_instance_id}/health',HttpMethod.GET,
                ServiceHealth.class, serviceInstanceId)
    }

    @Override
    ResponseEntity<Void> createOrUpdateServiceDefinition(String definition){
        return extendedExchange('/custom/admin/service-definition',HttpMethod.POST,
                createExtendedHttpEntity(definition),Void.class)
    }

    @Override
    ResponseEntity<Void> deleteServiceDefinition(String id){
        return extendedExchange('/custom/admin/service-definition/{id}',HttpMethod.DELETE,Void.class,id)
    }

    @Override
    ResponseEntity<BackupDto> createBackup(String serviceInstanceId){
        return extendedExchange('/custom/service_instances/{service_instance}/backups', HttpMethod.POST, BackupDto.class, serviceInstanceId)
    }

    @Override
    ResponseEntity<String> deleteBackup(String serviceInstanceId, String backupId){
        return extendedExchange("/custom/service_instances/{service_instance}/backups/{backup_id}", HttpMethod.DELETE, String.class, serviceInstanceId, backupId)
    }

    @Override
    ResponseEntity<BackupDto> getBackup(String serviceInstanceId, String backupId){
        return extendedExchange("/custom/service_instances/{service_instance}/backups/{backup_id}", HttpMethod.GET, BackupDto.class, serviceInstanceId, backupId)
    }

    @Override
    @TypeChecked(TypeCheckingMode.SKIP)
    ResponseEntity<List<BackupDto>> listBackups(String serviceInstanceId){
        return extendedExchange("/custom/service_instances/{service_instance}/backups", HttpMethod.GET, List.class, serviceInstanceId)
    }

    @Override
    ResponseEntity<RestoreDto> restoreBackup(String serviceInstanceId, String backupId) {
        return extendedExchange("/custom/service_instances/{service_instance}/backups/{backup_id}/restores",
                HttpMethod.POST, RestoreDto.class, serviceInstanceId, backupId)
    }

    @Override
    ResponseEntity<RestoreDto> getRestoreStatus(String serviceInstanceId, String backupId, String restoreId){
        return extendedExchange("/custom/service_instances/{service_instance}/backups/{backup_id}/restores/{restore_id}",
                HttpMethod.GET, RestoreDto.class, serviceInstanceId, backupId, restoreId)
    }

    @Override
    ResponseEntity<String> getApi(String serviceInstanceId){
        return extendedExchange("/custom/service_instances/{service_instance}/api-docs",
                HttpMethod.GET, String.class, serviceInstanceId)
    }
}