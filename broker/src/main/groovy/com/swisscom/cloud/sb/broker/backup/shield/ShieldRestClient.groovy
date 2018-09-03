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

package com.swisscom.cloud.sb.broker.backup.shield

import com.swisscom.cloud.sb.broker.backup.shield.dto.*
import com.swisscom.cloud.sb.broker.util.GsonFactory
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.springframework.http.*
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate

@Slf4j
class ShieldRestClient {
    public static final String HEADER_API_KEY = 'X-Shield-Token'
    public static final String HEADER_API_SESSION = 'X-Shield-Session'

    private ShieldConfig config
    private RestTemplate restTemplate
    private int apiVersion = 1

    ShieldRestClient(RestTemplate restTemplate, ShieldConfig shieldConfig) {
        this.restTemplate = restTemplate
        this.config = shieldConfig
        this.apiVersion = getAPIVersion()
    }

    int getAPIVersion() {
        try {
            this.apiVersion = 1
            def response = restTemplate.exchange(statusUrl(), HttpMethod.GET, configureRequestEntity(), String.class)
            String version = new JsonSlurper().parseText(response.body).version
            if (version != null && version[0..0].toInteger() == 1) return 1
            else {
                this.apiVersion = 2
                response = restTemplate.exchange(infoUrl(), HttpMethod.GET, configureRequestEntity(), String.class)
                if (new JsonSlurper().parseText(response.body).api == 2) return 2
            }
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                this.apiVersion = 2
                def response = restTemplate.exchange(infoUrl(), HttpMethod.GET, configureRequestEntity(), String.class)
                if (new JsonSlurper().parseText(response.body).api == 2) return 2
                throw e
            }
        }
    }

    String getTenantUuidByName(String name) {
        def response = restTemplate.exchange(tenantsUrl() + "?limit=1&name=${name}", HttpMethod.GET, configureRequestEntity(), String.class)
        return new JsonSlurper().parseText(response.body)[0].uuid
    }

    Object getStatus() {
        def response = restTemplate.exchange(statusUrl(), HttpMethod.GET, configureRequestEntity(), Object.class)
        return response.getBody()
    }

    StoreDto getStoreByName(String name) {
        getStore("?name=${name}")
    }

    StoreDto getStore(String arguments) {
        def stores = getStores(arguments)
        stores ? stores.first() : null
    }

    List<StoreDto> getStores(String arguments) {
        getResources(storesUrl() + arguments, StoreDto[].class)
    }

    RetentionDto getRetentionByName(String name) {
        getRetention("?name=${name}")
    }

    RetentionDto getRetention(String arguments) {
        def retentions = getRetentions(arguments)
        retentions ? retentions.first() : null
    }

    List<RetentionDto> getRetentions(String arguments) {
        getResources(retentionsUrl() + arguments, RetentionDto[].class)
    }

    ScheduleDto getScheduleByName(String name) {
        getSchedule("?name=${name}")
    }

    ScheduleDto getSchedule(String arguments) {
        def schedules = getSchedules(arguments)
        schedules ? schedules.first() : null
    }

    List<ScheduleDto> getSchedules(String arguments) {
        getResources(schedulesUrl() + arguments, ScheduleDto[].class)
    }

    TargetDto getTargetByName(String name) {
        getTarget("?name=${name}")
    }

    TargetDto getTarget(String arguments) {
        def targets = getTargets(arguments)
        targets ? targets.first() : null
    }

    List<TargetDto> getTargets(String arguments) {
        getResources(targetsUrl() + arguments, TargetDto[].class)
    }

    String createTarget(String targetName, ShieldTarget target, String agent) {
        def body = [name    : targetName,
                    plugin  : target.pluginName(),
                    endpoint: target.endpointJson(),
                    agent   : agent]

        def response = restTemplate.exchange(targetsUrl(), HttpMethod.POST, configureRequestEntity(body), String.class)

        new JsonSlurper().parseText(response.body).uuid
    }

    String updateTarget(TargetDto existingTarget, ShieldTarget target, String agent) {
        def body = [name    : existingTarget.name,
                    summary : existingTarget.summary,
                    plugin  : target.pluginName(),
                    endpoint: target.endpointJson(),
                    agent   : agent]
        restTemplate.exchange(targetUrl(existingTarget.uuid), HttpMethod.PUT, configureRequestEntity(body), (Class) null)

        existingTarget.uuid
    }

    void deleteTarget(String uuid) {
        restTemplate.exchange(targetUrl(uuid), HttpMethod.DELETE, configureRequestEntity((String) null), String.class)
    }

    JobDto getJobByName(String name) {
        getJob("?name=${name}")
    }

    JobDto getJobByUuid(String uuid) {
        getResources(jobUrl(uuid), JobDto.class).first()
    }

    JobDto getJob(String arguments) {
        def jobs = getJobs(arguments)
        jobs ? jobs.first() : null
    }

    List<JobDto> getJobs(String arguments) {
        getResources(jobsUrl() + arguments, JobDto[].class)
    }

    String createJob(String jobName,
                     String targetUuid,
                     String storeUuid,
                     String retentionUuid,
                     String scheduleUuid,
                     boolean paused = true) {
        def body = [name     : jobName,
                    target   : targetUuid,
                    store    : storeUuid,
                    retention: retentionUuid,
                    schedule : scheduleUuid,
                    paused   : paused]

        if (apiVersion == 2) {
            body = replaceRetentionWithPolicy(body, retentionUuid)
        }
        def response = restTemplate.exchange(jobsUrl(), HttpMethod.POST, configureRequestEntity(body), String.class)
        new JsonSlurper().parseText(response.body).uuid
    }

    String updateJob(JobDto existingJob,
                     String targetUuid,
                     String storeUuid,
                     String retentionUuid,
                     String scheduleUuid,
                     boolean paused = true) {
        def body = [name     : existingJob.name,
                    summary  : existingJob.summary,
                    target   : targetUuid,
                    store    : storeUuid,
                    retention: retentionUuid,
                    schedule : scheduleUuid,
                    paused   : paused]
        if (apiVersion == 2) {
            body = replaceRetentionWithPolicy(body, retentionUuid)
        }
        restTemplate.exchange(jobUrl(existingJob.uuid), HttpMethod.PUT, configureRequestEntity(body), (Class) null)
        existingJob.uuid
    }

    String runJob(String uuid) {
        def response = restTemplate.exchange(jobUrl(uuid) + "/run", HttpMethod.POST, configureRequestEntity(), String.class)
        new JsonSlurper().parseText(response.body).task_uuid
    }

    void deleteJob(String uuid) {
        restTemplate.exchange(jobUrl(uuid), HttpMethod.DELETE, configureRequestEntity(), (Class) null)
    }

    TaskDto getTaskByUuid(String uuid) {
        def response = restTemplate.exchange(taskUrl(uuid), HttpMethod.GET, configureRequestEntity(), String.class)
        def dto = GsonFactory.withISO8601Datetime().fromJson(response.body, TaskDto)
        dto.typeParsed = TaskDto.Type.of(dto.type)
        dto.statusParsed = TaskDto.Status.of(dto.status)
        dto
    }

    void deleteTaskByUuid(String uuid) {
        restTemplate.exchange(taskUrl(uuid), HttpMethod.DELETE, configureRequestEntity(), String.class)
    }

    ArchiveDto getArchiveByUuid(String uuid) {
        def response = restTemplate.exchange(archiveUrl(uuid), HttpMethod.GET, configureRequestEntity(), String.class)
        def dto = GsonFactory.withISO8601Datetime().fromJson(response.body.toString(), ArchiveDto)
        dto.statusParsed = ArchiveDto.Status.of(dto.status)
        dto
    }

    String restoreArchive(String uuid) {
        def response = restTemplate.exchange(archiveUrl(uuid) + "/restore", HttpMethod.POST, configureRequestEntity(), String.class)
        new JsonSlurper().parseText(response.body).task_uuid
    }

    void deleteArchive(String uuid) {
        restTemplate.exchange(archiveUrl(uuid), HttpMethod.DELETE, configureRequestEntity(), String.class)
    }

    private <T> List<T> getResources(String endpoint, final Class<T[]> clazz) {
        def response = restTemplate.exchange(endpoint, HttpMethod.GET, configureRequestEntity(), String.class)
        final T[] jsonToObject = GsonFactory.withISO8601Datetime().fromJson(response.body.toString(), clazz)
        return Arrays.asList(jsonToObject)
    }


    private <T> HttpEntity<T> configureRequestEntity(T t) {
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
        apiVersion == 2 ? headers.add(HEADER_API_SESSION, getSession()) : headers.add(HEADER_API_KEY, config.apiKey)
        HttpEntity<T> entity = t ? new HttpEntity<T>(t, headers) : new HttpEntity<T>(headers)
        return entity
    }

    protected String getSession() {
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
        def body = [username: config.username,
                    password: config.password]
        HttpEntity<Map<String,String>> request = new HttpEntity<Map<String,String>>(body, headers)
        ResponseEntity<String> response = restTemplate.exchange(loginUrl(), HttpMethod.POST, request, String.class)
        return response.getHeaders().getValuesAsList(HEADER_API_SESSION)[0]
    }

    protected String storesUrl() {
        "${tenantBasedUrl()}/stores"
    }

    protected String retentionsUrl() {
        apiVersion == 2 ? "${tenantBasedUrl()}/policies" : "${tenantBasedUrl()}/retention"
    }

    protected String schedulesUrl() {
        "${tenantBasedUrl()}/schedules"
    }

    protected String taskUrl(String uuid) {
        "${tenantBasedUrl()}/task/${uuid}"
    }

    protected String archiveUrl(String uuid) {
        "${tenantBasedUrl()}/archive/${uuid}"
    }

    protected String targetsUrl() {
        "${tenantBasedUrl()}/targets"
    }

    protected String targetUrl(String uuid) {
        "${tenantBasedUrl()}/target/${uuid}"
    }

    protected String jobsUrl() {
        "${tenantBasedUrl()}/jobs"
    }

    protected String jobUrl(String uuid) {
        "${tenantBasedUrl()}/job/${uuid}"
    }

    protected String statusUrl() {
        "${baseUrl()}/status"
    }

    protected String infoUrl() {
        "${baseUrl()}/info"
    }

    protected String tenantsUrl() {
        "${baseUrl()}/tenants"
    }

    protected String loginUrl() {
        "${baseUrl()}/auth/login"
    }

    private String tenantBasedUrl() {
        apiVersion == 2 ? "${baseUrl()}/tenants/${getTenantUuidByName(config.defaultTenantName)}" : baseUrl()
    }

    private String baseUrl() {
        "${config.baseUrl}/v${apiVersion}"
    }

    private Map<String,String> replaceRetentionWithPolicy(Map<?, ?> body, String retention) {
        def iterator = body.entrySet().iterator()
        while (iterator.hasNext()) {
            if (iterator.next().key == "retention") {
                iterator.remove()
            }
        }
        body.put("policy", retention)
        return body
    }
}
