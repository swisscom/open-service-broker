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
import io.github.resilience4j.retry.RetryConfig
import io.vavr.control.Try
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.util.Assert
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate

import java.time.Duration
import java.util.function.Function
import java.util.function.Supplier

import static com.google.common.base.Preconditions.checkArgument
import static io.github.resilience4j.retry.IntervalFunction.ofExponentialBackoff
import static io.github.resilience4j.retry.RetryConfig.custom
import static org.apache.commons.lang.StringUtils.isNotBlank
import static org.springframework.http.HttpMethod.*

@PackageScope
class ShieldRestClientV1 implements ShieldRestClient {
    private static final Logger LOG = LoggerFactory.getLogger(ShieldRestClientV1.class)
    private static final String RETRY_NAME = "shield-api-client-default"

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
        this.restTemplate = hackForResolvingTheShieldBadSupportedMediaBug(new RestTemplateBuilder().build())
        this.config = shieldConfig
        initRetry(shieldConfig.maxNumberOfApiRetries, shieldConfig.waitBetweenApiRetries)
    }

    private void initRetry(int maxNumberOfRetries, Duration waitBetweenRetries) {
        this.defaultRetry = Retry.of(RETRY_NAME, initRetryConfiguration(maxNumberOfRetries, waitBetweenRetries))
        initRetryEventsListeners()
    }


    private static RetryConfig initRetryConfiguration(int maxNumberOfRetryAttempts,
                                                      Duration durationBetweenRetryAttempts) {
        return custom().
                maxAttempts(maxNumberOfRetryAttempts).
                waitDuration(durationBetweenRetryAttempts).
                intervalFunction(ofExponentialBackoff()).
                ignoreExceptions(HttpClientErrorException.class,
                                 IllegalArgumentException.class,
                                 HttpServerErrorException.NotImplemented.class).
                retryExceptions(Exception.class).
                build()
    }

    private void initRetryEventsListeners() {
        this.defaultRetry.getEventPublisher()
                         .onRetry({e ->
                             LOG.debug("Retrying {} because received error '{}': {}",
                                       e.getNumberOfRetryAttempts(),
                                       e.getLastThrowable().getClass().getSimpleName(),
                                       e.getLastThrowable().getMessage())
                         })
    }

    /**
     * All calls to shield API methods MUST be executed using this {@link #execute(java.util.function.Supplier, java.util.function.Function)} for being retried
     * in case shieldApiException failing.
     *
     * @param <R >
     * @param toExecute
     * @param exceptionSupplier
     * @return the intended object or collection shieldApiException objects returned by toExecute
     */
    private <R> R execute(Supplier<R> toExecute,
                          Function<? super Throwable, RuntimeException> exceptionSupplier) {
        LOG.debug("Executing request to {}", baseUrl())
        return Try.ofSupplier(Retry.decorateSupplier(this.defaultRetry, toExecute))
                  .getOrElseThrow(exceptionSupplier)
    }

    Object getStatus() {
        execute({-> restTemplate.exchange(statusUrl(), GET, configureRequestEntity(), Object.class).getBody()},
                {ex -> ShieldApiException.of("Failed to get status", ex)})
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
                {->
                    restTemplate.exchange(storesUrl() + arguments,
                                          GET,
                                          configureRequestEntity(),
                                          StoreDto[].class).getBody()
                },
                {ex -> ShieldApiException.of("Failed to get list of stores", ex, arguments)}
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
        execute(
                {->
                    restTemplate.exchange(retentionsUrl() + arguments,
                                          GET,
                                          configureRequestEntity(),
                                          RetentionDto[].class).getBody()
                }, {ex -> ShieldApiException.of("Failed to get list of retentions", ex, arguments)}
        )
    }

    ScheduleDto getScheduleByName(String name) {
        getSchedule("?name=${name}")
    }

    ScheduleDto getSchedule(String arguments) {
        def schedules = getSchedules(arguments)
        schedules ? schedules.first() : null
    }

    List<ScheduleDto> getSchedules(String arguments) {
        execute(
                {->
                    restTemplate.exchange(schedulesUrl() + arguments,
                                          GET,
                                          configureRequestEntity(),
                                          ScheduleDto[].class).getBody()
                },
                {ex -> ShieldApiException.of("Failed to get list of schedules", ex, arguments)})
    }

    TargetDto getTargetByName(String name) {
        getTarget("?name=${name}")
    }

    @Override
    Collection<TargetDto> getTargetsByName(String name) {
        checkArgument(isNotBlank(name), "Target name can not be empty")
        getTargets("?name=${name}")
    }

    TargetDto getTarget(String arguments) {
        def targets = getTargets(arguments)
        targets ? targets.first() : null
    }

    List<TargetDto> getTargets(String arguments) {
        execute({->
                    restTemplate.exchange(targetsUrl() + arguments, GET, configureRequestEntity(), TargetDto[].class).
                            getBody()
                }, {ex -> ShieldApiException.of("Failed to get list of targets", ex, arguments)})

    }

    UUID createTarget(String targetName, ShieldTarget target, String agent) {
        execute({->
                    def body = [name    : targetName,
                                plugin  : target.pluginName(),
                                endpoint: target.endpointJson(),
                                agent   : agent]
                    restTemplate.exchange(targetsUrl(), POST, configureRequestEntity(body), CreateResponseDto.class).
                            getBody().
                            getUuid()
                }, {ex -> ShieldApiException.of("Failed to create target", ex)})
    }

    UUID updateTarget(TargetDto existingTarget, ShieldTarget target, String agent) {
        execute({->
                    def body = [name    : existingTarget.name,
                                summary : existingTarget.summary,
                                plugin  : target.pluginName(),
                                endpoint: target.endpointJson(),
                                agent   : agent]
                    restTemplate.exchange(targetUrl(existingTarget.uuid),
                                          PUT,
                                          configureRequestEntity(body),
                                          (Class) null)
                }, {ex -> ShieldApiException.of("Failed to update target", ex)})

        existingTarget.uuid
    }

    void deleteTarget(UUID uuid) {
        execute({->
                    restTemplate.exchange(targetUrl(uuid),
                                          DELETE,
                                          configureRequestEntity((String) null),
                                          String.class)
                }, {ex -> ShieldApiException.of("Failed to delete target", ex, uuid.toString())})

    }

    JobDto getJobByName(String name) {
        getJob("?name=${name}")
    }

    JobDto getJobByUuid(UUID uuid) {
        execute({-> restTemplate.exchange(jobUrl(uuid), GET, configureRequestEntity(), JobDto[].class).getBody().first()
                }, {ex -> ShieldApiException.of("Failed to get job", ex, uuid.toString())})

    }

    @Override
    Collection<JobDto> getJobsByName(String name) {
        checkArgument(isNotBlank(name), "Job name can not be empty")
        getJobs("?name=${name}")
    }

    JobDto getJob(String arguments) {
        def jobs = getJobs(arguments)
        jobs ? jobs.first() : null
    }

    List<JobDto> getJobs(String arguments) {
        execute({->
                    restTemplate.exchange(jobsUrl() + arguments, GET, configureRequestEntity(), JobDto[].class).
                            getBody()
                }, {ex -> ShieldApiException.of("Failed to get list of jobs", ex, arguments)})

    }

    UUID createJob(String jobName,
                   UUID targetUuid,
                   UUID storeUuid,
                   UUID retentionUuid,
                   UUID scheduleUuid,
                   boolean paused = true) {
        execute({->
                    def body = getCreateJobBody(jobName, targetUuid, storeUuid, retentionUuid, scheduleUuid, paused)

                    restTemplate.exchange(jobsUrl(), POST, configureRequestEntity(body), CreateResponseDto.class).
                            getBody().
                            getUuid()
                }, {ex -> ShieldApiException.of("Failed to create job", ex)})

    }

    UUID updateJob(JobDto existingJob,
                   UUID targetUuid,
                   UUID storeUuid,
                   UUID retentionUuid,
                   UUID scheduleUuid,
                   boolean paused = true) {
        execute({->
                    def body = getUpdateJobBody(existingJob, targetUuid, storeUuid, retentionUuid, scheduleUuid, paused)
                    restTemplate.exchange(jobUrl(existingJob.uuid), PUT, configureRequestEntity(body), (Class) null)
                },
                {ex -> ShieldApiException.of("Failed to update job", ex)})

        existingJob.uuid
    }

    UUID runJob(UUID uuid) {
        execute({->
                    restTemplate.exchange(jobUrl(uuid) + "/run", POST, configureRequestEntity(), TaskResponseDto.class).
                            getBody().
                            getTaskUuid()
                }, {ex -> ShieldApiException.of("Failed to run backup", ex, uuid.toString())})
    }

    void deleteJob(UUID uuid) {
        execute({-> restTemplate.exchange(jobUrl(uuid), DELETE, configureRequestEntity(), (Class) null)},
                {ex -> ShieldApiException.of("Failed to delete job", ex, uuid.toString())})

    }

    TaskDto getTaskByUuid(UUID uuid) {
        execute({-> restTemplate.exchange(taskUrl(uuid), GET, configureRequestEntity(), TaskDto.class).getBody()},
                {ex -> ShieldApiException.of("Failed to get task", ex, uuid.toString())})
    }

    void deleteTaskByUuid(UUID uuid) {
        execute({-> restTemplate.exchange(taskUrl(uuid), DELETE, configureRequestEntity(), String.class)},
                {ex -> ShieldApiException.of("Failed to delete task", ex, uuid.toString())})

    }

    ArchiveDto getArchiveByUuid(UUID uuid) {
        execute({-> restTemplate.exchange(archiveUrl(uuid), GET, configureRequestEntity(), ArchiveDto.class).getBody()},
                {ex -> ShieldApiException.of("Failed to get archive", ex, uuid.toString())})

    }

    String restoreArchive(UUID uuid) {
        execute({->
                    restTemplate.exchange(archiveUrl(uuid) + "/restore",
                                          POST,
                                          configureRequestEntity(),
                                          TaskResponseDto.class).getBody().getTaskUuid()
                }, {ex -> ShieldApiException.of("Failed to restore archive", ex, uuid.toString())})

    }

    void deleteArchive(UUID uuid) {
        execute({-> restTemplate.exchange(archiveUrl(uuid), DELETE, configureRequestEntity(), String.class)},
                {ex -> ShieldApiException.of("Failed to delete archive", ex, uuid.toString())})

    }

    private static Map<String, ?> getCreateJobBody(String jobName,
                                                   UUID targetUuid,
                                                   UUID storeUuid,
                                                   UUID retentionUuid,
                                                   UUID scheduleUuid,
                                                   boolean paused) {
        [name     : jobName,
         target   : targetUuid.toString(),
         store    : storeUuid.toString(),
         retention: retentionUuid.toString(),
         schedule : scheduleUuid.toString(),
         paused   : paused]
    }

    private static Map<String, ?> getUpdateJobBody(JobDto existingJob,
                                                   UUID targetUuid,
                                                   UUID storeUuid,
                                                   UUID retentionUuid,
                                                   UUID scheduleUuid,
                                                   boolean paused = true) {
        [name     : existingJob.name,
         summary  : existingJob.summary,
         target   : targetUuid.toString(),
         store    : storeUuid.toString(),
         retention: retentionUuid.toString(),
         schedule : scheduleUuid.toString(),
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

    private String taskUrl(UUID uuid) {
        "${baseUrl()}/task/${uuid.toString()}"
    }

    private String archiveUrl(UUID uuid) {
        "${baseUrl()}/archive/${uuid.toString()}"
    }

    private String targetsUrl() {
        "${baseUrl()}/targets"
    }

    private String targetUrl(UUID uuid) {
        "${baseUrl()}/target/${uuid.toString()}"
    }

    private String jobsUrl() {
        "${baseUrl()}/jobs"
    }

    private String jobUrl(UUID uuid) {
        "${baseUrl()}/job/${uuid.toString()}"
    }

    private String statusUrl() {
        "${baseUrl()}/status"
    }

    /**
     * Support text/plain Content-Type for JSON parsing, because SHIELD API sets wrong Content-Type
     */
    private static RestTemplate hackForResolvingTheShieldBadSupportedMediaBug(RestTemplate restTemplate) {
        HttpMessageConverter converter = new MappingJackson2HttpMessageConverter()
        converter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON,
                                                       MediaType.TEXT_PLAIN))
        restTemplate.getMessageConverters().add(converter)
        return restTemplate
    }
}
