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
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import groovy.transform.PackageScope
import io.github.resilience4j.retry.Retry
import io.vavr.control.Try
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.util.Assert
import org.springframework.web.client.RestTemplate

import java.util.function.Function
import java.util.function.Supplier

@PackageScope
class ShieldRestClientV1 implements ShieldRestClient {
    private static final Logger LOG = LoggerFactory.getLogger(ShieldRestClientV1.class)
    private static final String RETRY_NAME = "shield-api-client-default";

    private static final String HEADER_API_KEY = 'X-Shield-Token'
    private static final int apiVersion = 1
    private final ShieldConfig config
    private final RestTemplate restTemplate

    private Retry defaultRetry

    static ShieldRestClientV1 of(ShieldConfig config) {
        return new ShieldRestClientV1(config)
    }

    private ShieldRestClientV1(ShieldConfig shieldConfig) {
        Assert.notNull(shieldConfig, "Shield config cannot be null!")
        this.restTemplate = addCustomRestTemplateConfig(new RestTemplateBuilder().build())
        this.config = shieldConfig
        initRetry()
    }

    private void initRetry() {
        this.defaultRetry = Retry.ofDefaults(RETRY_NAME);
        initRetryEventsListeners();
    }

    private void initRetryEventsListeners() {
        this.defaultRetry.getEventPublisher()
                         .onRetry({e -> LOG.debug("Retrying {} because received error '{}': {}",
                                                  e.getNumberOfRetryAttempts(),
                                                  e.getLastThrowable().getClass().getSimpleName(),
                                                  e.getLastThrowable().getMessage())});
    }

    /**
     * All calls to shield API methods MUST be executed using this {@link #execute(java.util.function.Supplier, java.util.function.Function)} for being retried
     * in case nsxApiException failing.
     *
     * @param <R>
     * @param toExecute
     * @param exceptionSupplier
     * @return the intended NSX object or collection nsxApiException objects returned by toExecute
     */
    private <R> R execute(Supplier<R> toExecute,
                          Function<? super Throwable, RuntimeException> exceptionSupplier) {
        LOG.debug("Executing request to {}", baseUrl());
        return Try.ofSupplier(Retry.decorateSupplier(this.defaultRetry, toExecute))
                  .getOrElseThrow(exceptionSupplier);
    }


    Object getStatus() {
        restTemplate.exchange(statusUrl(), HttpMethod.GET, configureRequestEntity(), Object.class).getBody()
    }

    StoreDto getStoreByName(String name) {
        getStore("?name=${name}")
    }

    StoreDto getStore(String arguments) {
        def stores = getStores(arguments)
        stores ? stores.first() : null
    }

    List<StoreDto> getStores(String arguments) {
        execute(
                { -> restTemplate.exchange(storesUrl() + arguments, HttpMethod.GET, configureRequestEntity(), StoreDto[].class).getBody()},
                {ex -> listShieldApiException(StoreDto[].class, ex, arguments)}
        )
    }

    RetentionDto getRetentionByName(String name) {
        getRetention("?name=${name}")
    }

    RetentionDto getRetention(String arguments) {
        def retentions = getRetentions(arguments)
        retentions ? retentions.first() : null
    }

    List<RetentionDto> getRetentions(String arguments) {
        restTemplate.exchange(retentionsUrl() + arguments,
                              HttpMethod.GET,
                              configureRequestEntity(),
                              RetentionDto[].class).getBody()
    }

    ScheduleDto getScheduleByName(String name) {
        getSchedule("?name=${name}")
    }

    ScheduleDto getSchedule(String arguments) {
        def schedules = getSchedules(arguments)
        schedules ? schedules.first() : null
    }

    List<ScheduleDto> getSchedules(String arguments) {
        restTemplate.exchange(schedulesUrl() + arguments,
                              HttpMethod.GET,
                              configureRequestEntity(),
                              ScheduleDto[].class).getBody()
    }

    TargetDto getTargetByName(String name) {
        getTarget("?name=${name}")
    }

    TargetDto getTarget(String arguments) {
        def targets = getTargets(arguments)
        targets ? targets.first() : null
    }

    List<TargetDto> getTargets(String arguments) {
        restTemplate.exchange(targetsUrl() + arguments, HttpMethod.GET, configureRequestEntity(), TargetDto[].class).
                getBody()
    }

    String createTarget(String targetName, ShieldTarget target, String agent) {
        def body = [name    : targetName,
                    plugin  : target.pluginName(),
                    endpoint: target.endpointJson(),
                    agent   : agent]

        restTemplate.exchange(targetsUrl(), HttpMethod.POST, configureRequestEntity(body), CreateResponseDto.class).
                getBody().
                getUuid()
    }

    String updateTarget(TargetDto existingTarget, ShieldTarget target, String agent) {
        def body = [name    : existingTarget.name,
                    summary : existingTarget.summary,
                    plugin  : target.pluginName(),
                    endpoint: target.endpointJson(),
                    agent   : agent]
        restTemplate.exchange(targetUrl(existingTarget.uuid),
                              HttpMethod.PUT,
                              configureRequestEntity(body),
                              (Class) null)

        existingTarget.uuid
    }

    void deleteTarget(String uuid) {
        restTemplate.exchange(targetUrl(uuid), HttpMethod.DELETE, configureRequestEntity((String) null), String.class)
    }

    JobDto getJobByName(String name) {
        getJob("?name=${name}")
    }

    JobDto getJobByUuid(String uuid) {
        restTemplate.exchange(jobUrl(uuid), HttpMethod.GET, configureRequestEntity(), JobDto[].class).getBody().first()
    }

    JobDto getJob(String arguments) {
        def jobs = getJobs(arguments)
        jobs ? jobs.first() : null
    }

    List<JobDto> getJobs(String arguments) {
        restTemplate.exchange(jobsUrl() + arguments, HttpMethod.GET, configureRequestEntity(), JobDto[].class).getBody()
    }

    String createJob(String jobName,
                     String targetUuid,
                     String storeUuid,
                     String retentionUuid,
                     String scheduleUuid,
                     boolean paused = true) {
        def body = getCreateJobBody(jobName, targetUuid, storeUuid, retentionUuid, scheduleUuid, paused)
        restTemplate.exchange(jobsUrl(), HttpMethod.POST, configureRequestEntity(body), CreateResponseDto.class).
                getBody().
                getUuid()
    }

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

    String runJob(String uuid) {
        restTemplate.exchange(jobUrl(uuid) + "/run",
                              HttpMethod.POST,
                              configureRequestEntity(),
                              TaskResponseDto.class).getBody().getTaskUuid()
    }

    void deleteJob(String uuid) {
        restTemplate.exchange(jobUrl(uuid), HttpMethod.DELETE, configureRequestEntity(), (Class) null)
    }

    TaskDto getTaskByUuid(String uuid) {
        restTemplate.exchange(taskUrl(uuid), HttpMethod.GET, configureRequestEntity(), TaskDto.class).getBody()
        /*def dto = GsonFactory.withISO8601Datetime().fromJson(response.body, TaskDto)
        dto.typeParsed = TaskDto.Type.of(dto.type)
        dto.statusParsed = TaskDto.Status.of(dto.status)
        dto*/
    }

    void deleteTaskByUuid(String uuid) {
        restTemplate.exchange(taskUrl(uuid), HttpMethod.DELETE, configureRequestEntity(), String.class)
    }

    ArchiveDto getArchiveByUuid(String uuid) {
        restTemplate.exchange(archiveUrl(uuid), HttpMethod.GET, configureRequestEntity(), ArchiveDto.class).getBody()
    }

    String restoreArchive(String uuid) {
        restTemplate.exchange(archiveUrl(uuid) + "/restore",
                              HttpMethod.POST,
                              configureRequestEntity(),
                              TaskResponseDto.class).getBody().getTaskUuid()
    }

    void deleteArchive(String uuid) {
        restTemplate.exchange(archiveUrl(uuid), HttpMethod.DELETE, configureRequestEntity(), String.class)
    }

    private static Map<String, ?> getCreateJobBody(String jobName,
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

    private static Map<String, ?> getUpdateJobBody(JobDto existingJob,
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

    private <T> HttpEntity<T> configureRequestEntity(T t) {
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
        headers.add(HEADER_API_KEY, config.apiKey)
        HttpEntity<T> entity = t ? new HttpEntity<T>(t, headers) : new HttpEntity<T>(headers)
        return entity
    }

    private String baseUrl() {
        "${config.baseUrl}/v${apiVersion}"
    }

    private String storesUrl() {
        "${baseUrl()}/stores"
    }

    private String retentionsUrl() {
        "${baseUrl()}/retention"
    }

    private String schedulesUrl() {
        "${baseUrl()}/schedules"
    }

    private String taskUrl(String uuid) {
        "${baseUrl()}/task/${uuid}"
    }

    private String archiveUrl(String uuid) {
        "${baseUrl()}/archive/${uuid}"
    }

    private String targetsUrl() {
        "${baseUrl()}/targets"
    }

    private String targetUrl(String uuid) {
        "${baseUrl()}/target/${uuid}"
    }

    private String jobsUrl() {
        "${baseUrl()}/jobs"
    }

    private String jobUrl(String uuid) {
        "${baseUrl()}/job/${uuid}"
    }

    private String statusUrl() {
        "${baseUrl()}/status"
    }

    private static RestTemplate addCustomRestTemplateConfig(RestTemplate restTemplate) {
        restTemplate.setErrorHandler(new ShieldRestResponseErrorHandler());

        // Support text/plain Content-Type for JSON parsing, because SHIELD API sets wrong Content-Type
        HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON,
                                                       MediaType.TEXT_PLAIN));
        restTemplate.getMessageConverters().add(converter);
        return restTemplate
    }
}
