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

package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import com.swisscom.cloud.sb.model.backup.BackupDto
import com.swisscom.cloud.sb.model.backup.RestoreDto
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

class BackupRestoreHelper {
    private String backupUrl
    private String restoreUrl
    private String cfExtUser
    private String cfExtPassword

    BackupRestoreHelper(String appBaseUrl, String cfExtUser, String cfExtPassword) {
        this.backupUrl = appBaseUrl + "/custom/service_instances/@@service_instance@@/backups/@@backup@@"
        this.restoreUrl = appBaseUrl + "/custom/service_instances/@@service_instance@@/backups/@@backup@@/restores/@@restore@@"
        this.cfExtUser = cfExtUser
        this.cfExtPassword = cfExtPassword
    }

    ResponseEntity<BackupDto> createBackup(String serviceInstanceId) {
        String url = backupUrl.replace("@@service_instance@@", serviceInstanceId).replace("/@@backup@@", "")
        return createRestTemplate().exchange(url, HttpMethod.POST, HttpEntity.EMPTY, BackupDto.class)
    }


    ResponseEntity<BackupDto> getBackup(String serviceInstanceId, String backupId) {
        String url = backupUrl
                .replace("@@service_instance@@", serviceInstanceId).replace("@@backup@@", backupId)
        return createRestTemplate().exchange(url, HttpMethod.GET, HttpEntity.EMPTY, BackupDto.class)
    }

    ResponseEntity<RestoreDto> restoreBackup(String serviceInstanceId, String backupId) {
        String url = restoreUrl
                .replace("@@service_instance@@", serviceInstanceId).replace("@@backup@@", backupId).replace("/@@restore@@", "")
        return createRestTemplate().exchange(url, HttpMethod.POST, HttpEntity.EMPTY, RestoreDto.class)
    }

    ResponseEntity<RestoreDto> getRestore(String serviceInstanceId, String backupId, String restoreId) {
        String url = restoreUrl
                .replace("@@service_instance@@", serviceInstanceId).replace("@@backup@@", backupId).replace("@@restore@@", restoreId)
        return createRestTemplate().exchange(url, HttpMethod.GET, HttpEntity.EMPTY, RestoreDto.class)
    }

    ResponseEntity deleteBackup(String serviceInstanceId, String backupId) {
        String url = backupUrl
                .replace("@@service_instance@@", serviceInstanceId).replace("@@backup@@", backupId)
        return createRestTemplate().exchange(url, HttpMethod.DELETE, HttpEntity.EMPTY, Void.class)
    }

    private RestTemplate createRestTemplate() {
        return new RestTemplateBuilder().withBasicAuthentication(cfExtUser, cfExtPassword).build()
    }
}
