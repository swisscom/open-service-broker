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
import com.swisscom.cloud.sb.broker.backup.shield.ShieldResourceNotFoundException
import com.swisscom.cloud.sb.broker.backup.shield.dto.JobDto
import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component

/**
 * {@link ShieldRestClient} implementation for shield version 0.10.x. Shield 0.10.x is currently used
 * for multiple Systems.
 * See <a href="https://github.com/shieldproject/shield/tree/v0.10.9">Shield Version 0.10.9</a>
 */
@Component
class ShieldRestClientv1 extends ShieldRestClientImpl implements ShieldRestClient {
    private static final Logger LOG = LoggerFactory.getLogger(ShieldRestClientv1.class)
    public static final String HEADER_API_KEY = 'X-Shield-Token'
    final int apiVersion = 1

    @Autowired
    ShieldRestClientv1(ShieldConfig shieldConfig) {
        super(shieldConfig)
    }

    boolean matchVersion() {
        try {
            def response = restTemplate.exchange(statusUrl(), HttpMethod.GET, configureRequestEntity(), String.class)
            if (response.body != null && !response.body.isEmpty()) {
                def json = new JsonSlurper().parseText(response.body)
                if (json.version != null) return json.version =~ /0\.10(.*)/
            }
        } catch(ShieldResourceNotFoundException e) {
            LOG.info("Not shield API version v1")
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
