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
import groovy.transform.CompileStatic
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity

@CompileStatic
interface IServiceBrokerClientExtended extends IServiceBrokerClient {
    def <T> ResponseEntity<T> extendedExchange(String url, HttpMethod method, ParameterizedTypeReference<T> responseType, Object... uriVariables)
    def <T> ResponseEntity<T> extendedExchange(String url, HttpMethod method, HttpEntity<?> requestEntity, ParameterizedTypeReference<T> responseType, Object... uriVariables)
    def <T> ResponseEntity<T> extendedExchange(String url, HttpMethod method, Class<T> responseType, Object... uriVariables)
    def <T> ResponseEntity<T> extendedExchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables)
    ResponseEntity<Endpoint> getEndpoint(String serviceInstanceId)
    ResponseEntity<ServiceUsage> getUsage(String serviceInstanceId)
    ResponseEntity<Set<ServiceUsage>> getExtendedUsage(String serviceInstanceId)
    ResponseEntity<ServiceHealth> getHealth(String serviceInstanceId)
    ResponseEntity<BackupDto> createBackup(String serviceInstanceId)
    ResponseEntity<String> deleteBackup(String serviceInstanceId, String backupId)
    ResponseEntity<BackupDto> getBackup(String serviceInstanceId, String backupId)
    ResponseEntity<List<BackupDto>> listBackups(String serviceInstanceId)
    ResponseEntity<RestoreDto> restoreBackup(String serviceInstanceId, String backupId)
    ResponseEntity<RestoreDto> getRestoreStatus(String serviceInstanceId, String backupId, String restore_id)
    ResponseEntity<String> getApi(String serviceInstanceId)
}
