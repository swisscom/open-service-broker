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
    @TypeChecked(TypeCheckingMode.SKIP)
    ResponseEntity<Set<ServiceUsageItem>> getExtendedUsage(String serviceInstanceId) {
        return restTemplate.exchange(appendPath('/v2/service_instances/{service_instance_id}/usage'),HttpMethod.GET,
                new HttpEntity(createSimpleAuthHeaders(cfUsername,cfPassword)),
                Set.class,serviceInstanceId)
    }

    @Override
    ResponseEntity<ServiceHealth> getHealth(String serviceInstanceId) {
        return restTemplate.exchange(appendPath('/v2/service_instances/{service_instance_id}/health'),HttpMethod.GET,
                new HttpEntity(createSimpleAuthHeaders(cfUsername,cfPassword)),
                ServiceHealth.class, serviceInstanceId)
    }

    @Override
    ResponseEntity<Void> createOrUpdateServiceDefinition(String definition){
        return restTemplate.exchange(appendPath('/custom/admin/service-definition'),HttpMethod.POST,
                new HttpEntity(definition, addJsonContentTypeHeader(createSimpleAuthHeaders(cfExtUsername,cfExtPassword))),Void.class)
    }

    @Override
    ResponseEntity<Void> deleteServiceDefinition(String id){
        return restTemplate.exchange(appendPath('/custom/admin/service-definition/{id}'),HttpMethod.DELETE,
                new HttpEntity(createSimpleAuthHeaders(cfExtUsername,cfExtPassword)),Void.class,id)
    }

    @Override
    ResponseEntity<BackupDto> createBackup(String serviceInstanceId){
        return restTemplate.exchange(appendPath('/custom/service_instances/{service_instance}/backups'), HttpMethod.POST,
                new HttpEntity(createSimpleAuthHeaders(cfExtUsername,cfExtPassword)), BackupDto.class, serviceInstanceId)
    }

    @Override
    ResponseEntity<String> deleteBackup(String serviceInstanceId, String backupId){
        return restTemplate.exchange(appendPath("/custom/service_instances/{service_instance}/backups/{backup_id}"), HttpMethod.DELETE,
                new HttpEntity(createSimpleAuthHeaders(cfExtUsername, cfExtPassword)), String.class, serviceInstanceId, backupId)
    }

    @Override
    ResponseEntity<BackupDto> getBackup(String serviceInstanceId, String backupId){
        return restTemplate.exchange(appendPath("/custom/service_instances/{service_instance}/backups/{backup_id}"), HttpMethod.GET,
                new HttpEntity(createSimpleAuthHeaders(cfExtUsername, cfExtPassword)), BackupDto.class, serviceInstanceId, backupId)
    }

    @Override
    @TypeChecked(TypeCheckingMode.SKIP)
    ResponseEntity<List<BackupDto>> listBackups(String serviceInstanceId){
        return restTemplate.exchange(appendPath("/custom/service_instances/{service_instance}/backups"), HttpMethod.GET,
                new HttpEntity(createSimpleAuthHeaders(cfExtUsername, cfExtPassword)), List.class, serviceInstanceId)
    }

    @Override
    ResponseEntity<RestoreDto> restoreBackup(String serviceInstanceId, String backupId) {
        return restTemplate.exchange(appendPath("/custom/service_instances/{service_instance}/backups/{backup_id}/restores"),
                HttpMethod.POST, new HttpEntity(createSimpleAuthHeaders(cfExtUsername,cfExtPassword)), RestoreDto.class, serviceInstanceId, backupId)
    }

    @Override
    ResponseEntity<RestoreDto> getRestoreStatus(String serviceInstanceId, String backupId, String restoreId){
        return restTemplate.exchange(appendPath("/custom/service_instances/{service_instance}/backups/{backup_id}/restores/{restore_id}"),
                HttpMethod.GET, new HttpEntity(createSimpleAuthHeaders(cfExtUsername,cfExtPassword)), RestoreDto.class, serviceInstanceId, backupId, restoreId)
    }

    @Override
    ResponseEntity<String> getApi(String serviceInstanceId){
        return restTemplate.exchange(appendPath("/custom/service_instances/{service_instance}/api-docs"),
                HttpMethod.GET, new HttpEntity(createSimpleAuthHeaders(cfExtUsername,cfExtPassword)), String.class, serviceInstanceId)
    }

    //dummy endpoints
    @Override
    ResponseEntity<String> lockUser(String serviceInstanceId){
        return restTemplate.exchange(appendPath("/custom/service_instances/{instanceId}/lock"),
                HttpMethod.PUT, new HttpEntity(createSimpleAuthHeaders(cfExtUsername,cfExtPassword)), String.class, serviceInstanceId)
    }

    //dummy endpoints
    @Override
    ResponseEntity<String> unlockUser(String serviceInstanceId){
        return restTemplate.exchange(appendPath("/custom/service_instances/{instanceId}/unlock"),
                HttpMethod.PUT, new HttpEntity(createSimpleAuthHeaders(cfExtUsername,cfExtPassword)), String.class, serviceInstanceId)
    }

    private HttpHeaders addJsonContentTypeHeader(HttpHeaders headers) {
        headers.add("Content-Type", "application/json")

        return headers
    }
}