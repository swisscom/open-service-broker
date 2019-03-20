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
import com.swisscom.cloud.sb.broker.backup.shield.dto.*
import com.swisscom.cloud.sb.broker.util.GsonFactory
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate

@Slf4j
abstract class ShieldRestClientImpl implements ShieldRestClient {
    protected ShieldConfig config
    protected RestTemplate restTemplate

    ShieldRestClientImpl(ShieldConfig shieldConfig, RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.withSSLValidationDisabled().build()
        this.restTemplate.setErrorHandler(new ShieldRestResponseErrorHandler())
        this.config = shieldConfig
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
        def body = getCreateJobBody(jobName, targetUuid, storeUuid, retentionUuid, scheduleUuid, paused)
        def response = restTemplate.exchange(jobsUrl(), HttpMethod.POST, configureRequestEntity(body), String.class)
        new JsonSlurper().parseText(response.body).uuid
    }

    abstract Map<String, ?> getCreateJobBody(String jobName, String targetUuid, String storeUuid, String retentionUuid, String scheduleUuid, boolean paused)

    String updateJob(JobDto existingJob,
                     String targetUuid,
                     String storeUuid,
                     String retentionUuid,
                     String scheduleUuid,
                     boolean paused = true) {
        def body = getUpdateJobBody(existingJob, targetUuid, storeUuid, retentionUuid, scheduleUuid, paused)
        restTemplate.exchange(jobUrl(existingJob.uuid), HttpMethod.PUT, configureRequestEntity(body), (Class) null)
        existingJob.uuid
    }

    abstract Map<String, ?> getUpdateJobBody(JobDto existingJob, String targetUuid, String storeUuid, String retentionUuid, String scheduleUuid, boolean paused = true)

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

    protected <T> List<T> getResources(String endpoint, final Class<T[]> clazz) {
        def response = restTemplate.exchange(endpoint, HttpMethod.GET, configureRequestEntity(), String.class)
        final T[] jsonToObject = GsonFactory.withISO8601Datetime().fromJson(response.body.toString(), clazz)
        return Arrays.asList(jsonToObject)
    }


    abstract protected <T> HttpEntity<T> configureRequestEntity(T t)

    protected String storesUrl() {
        "${baseUrl()}/stores"
    }

    protected String retentionsUrl() {
        "${baseUrl()}/retention"
    }

    protected String schedulesUrl() {
        "${baseUrl()}/schedules"
    }

    protected String taskUrl(String uuid) {
        "${baseUrl()}/task/${uuid}"
    }

    protected String archiveUrl(String uuid) {
        "${baseUrl()}/archive/${uuid}"
    }

    protected String targetsUrl() {
        "${baseUrl()}/targets"
    }

    protected String targetUrl(String uuid) {
        "${baseUrl()}/target/${uuid}"
    }

    protected String jobsUrl() {
        "${baseUrl()}/jobs"
    }

    protected String jobUrl(String uuid) {
        "${baseUrl()}/job/${uuid}"
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

    abstract protected String baseUrl()

}
