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
import com.swisscom.cloud.sb.broker.backup.shield.ShieldTarget
import com.swisscom.cloud.sb.broker.backup.shield.dto.JobDto
import com.swisscom.cloud.sb.broker.backup.shield.dto.ScheduleDto
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.stereotype.Component

@Slf4j
@Component
class ShieldRestClientv2 extends ShieldRestClientImpl implements ShieldRestClient {
    public static final String HEADER_API_SESSION = 'X-Shield-Session'
    final int apiVersion = 2

    @Autowired
    ShieldRestClientv2(ShieldConfig shieldConfig, RestTemplateBuilder restTemplateBuilder) {
        super(shieldConfig, restTemplateBuilder)
    }

    boolean matchVersion() {
        try {
            def response = restTemplate.exchange(infoUrl(), HttpMethod.GET, null, String.class)
            return parseAndCheckVersion(response.body)
        } catch(Exception e) {
            log.debug("Not shield API version v2")
        }
        return false
    }

    boolean parseAndCheckVersion(String body) {
        return new JsonSlurper().parseText(body).api == 2
    }

    String getTenantUuidByName(String name) {
        def response = restTemplate.exchange(tenantsUrl() + "?limit=1&name=${name}", HttpMethod.GET, configureRequestEntity(), String.class)
        return new JsonSlurper().parseText(response.body)[0].uuid
    }

    ScheduleDto getScheduleByName(String name) {
        null
    }

    String createTarget(String targetName, ShieldTarget target, String agent) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        def body = [name    : targetName,
                    plugin  : target.pluginName(),
                    config  : jsonSlurper.parseText(target.endpointJson()),
                    agent   : agent]
        def response = restTemplate.exchange(targetsUrl(), HttpMethod.POST, configureRequestEntity(body), String.class)
        new JsonSlurper().parseText(response.body).uuid
    }

    Map<String, ?> getCreateJobBody(String jobName, String targetUuid, String storeUuid, String policyUuid, String schedule, boolean paused) {
        [name     : jobName,
         target   : targetUuid,
         store    : storeUuid,
         policy   : policyUuid,
         schedule : schedule,
         paused   : paused]
    }

    Map<String, ?> getUpdateJobBody(JobDto existingJob, String targetUuid, String storeUuid, String policyUuid, String schedule, boolean paused = true) {
        [name     : existingJob.name,
         summary  : existingJob.summary,
         target   : targetUuid,
         store    : storeUuid,
         policy   : policyUuid,
         schedule : schedule,
         paused   : paused]
    }

    protected <T> HttpEntity<T> configureRequestEntity(T t) {
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
        headers.add(HEADER_API_SESSION, login())
        HttpEntity<T> entity = t ? new HttpEntity<T>(t, headers) : new HttpEntity<T>(headers)
        return entity
    }

    protected String login() {
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
        def body = [username: config.username,
                    password: config.password]
        HttpEntity<Map<String,String>> request = new HttpEntity<Map<String,String>>(body, headers)
        ResponseEntity<String> response = restTemplate.exchange(loginUrl(), HttpMethod.POST, request, String.class)
        return response.getHeaders().getValuesAsList(HEADER_API_SESSION)[0]
    }

    protected String statusUrl() {
        infoUrl()
    }

    protected String jobUrl(String uuid) {
        "${baseUrl()}/jobs/${uuid}"
    }

    protected String taskUrl(String uuid) {
        "${baseUrl()}/tasks/${uuid}"
    }

    protected String archiveUrl(String uuid) {
        "${baseUrl()}/archives/${uuid}"
    }

    protected String targetUrl(String uuid) {
        "${baseUrl()}/targets/${uuid}"
    }

    protected String infoUrl() {
        "${rootUrl()}/info"
    }

    protected String retentionsUrl() {
        "${baseUrl()}/policies"
    }

    protected String tenantsUrl() {
        "${rootUrl()}/tenants"
    }

    protected String loginUrl() {
        "${rootUrl()}/auth/login"
    }

    protected String baseUrl() {
         "${rootUrl()}/tenants/${getTenantUuidByName(config.defaultTenantName)}"
    }

    private String rootUrl() {
        "${config.baseUrl}/v${apiVersion}"
    }
}
