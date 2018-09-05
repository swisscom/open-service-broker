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

package com.swisscom.cloud.sb.broker.backup.shield.restClient

import com.swisscom.cloud.sb.broker.backup.shield.ShieldConfig
import com.swisscom.cloud.sb.broker.backup.shield.dto.JobDto
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component

@Slf4j
@Component
class ShieldRestClientv1 extends ShieldRestClientImpl implements ShieldRestClient {
    public static final String HEADER_API_KEY = 'X-Shield-Token'
    private final int apiVersion = 1

    @Autowired
    ShieldRestClientv1(ShieldConfig shieldConfig, ShieldRestTemplate restTemplate) {
        super(shieldConfig, restTemplate)
    }

    boolean matchVersion() {
        try {
            def response = restTemplate.exchange(statusUrl(), HttpMethod.GET, null, String.class)
            String version = new JsonSlurper().parseText(response.body).version
            return version[0..0].toInteger() == 1
        } catch(Exception e) {
            log.debug("Not shield API version v1")
        }
        return false
    }

    Map<String, ?> getCreateJobBody(String jobName,
                                    String targetUuid,
                                    String storeUuid,
                                    String retentionUuid,
                                    String scheduleUuid,
                                    boolean paused) {
        [name     : jobName,
         target   : targetUuid,
         store    : storeUuid,
         retention: retentionUuid,
         schedule : scheduleUuid,
         paused   : paused]
    }

    Map<String, ?> getUpdateJobBody(JobDto existingJob,
                                    String targetUuid,
                                    String storeUuid,
                                    String retentionUuid,
                                    String scheduleUuid,
                                    boolean paused = true) {
        [name     : existingJob.name,
         summary  : existingJob.summary,
         target   : targetUuid,
         store    : storeUuid,
         retention: retentionUuid,
         schedule : scheduleUuid,
         paused   : paused]
    }

    protected <T> HttpEntity<T> configureRequestEntity(T t) {
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
        headers.add(HEADER_API_KEY, config.apiKey)
        HttpEntity<T> entity = t ? new HttpEntity<T>(t, headers) : new HttpEntity<T>(headers)
        return entity
    }

    protected String baseUrl() {
        "${config.baseUrl}/v${apiVersion}"
    }
}
