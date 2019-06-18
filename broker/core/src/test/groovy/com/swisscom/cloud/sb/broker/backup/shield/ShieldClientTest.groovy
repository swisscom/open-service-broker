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

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.swisscom.cloud.sb.broker.async.job.JobStatus
import com.swisscom.cloud.sb.broker.backup.BackupPersistenceService
import com.swisscom.cloud.sb.broker.backup.shield.restClient.ShieldRestClientFactory
import com.swisscom.cloud.sb.broker.backup.shield.restClient.ShieldRestClientv1
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import groovy.json.JsonGenerator
import org.joda.time.LocalTime
import org.junit.ClassRule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.util.Assert
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Unroll

import static com.github.tomakehurst.wiremock.client.WireMock.recordSpec
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import static com.swisscom.cloud.sb.broker.backup.shield.BackupParameter.backupParameter
import static com.swisscom.cloud.sb.broker.util.servicedetail.ShieldServiceDetailKey.SHIELD_JOB_UUID
import static com.swisscom.cloud.sb.broker.util.servicedetail.ShieldServiceDetailKey.SHIELD_TARGET_UUID

@Stepwise
class ShieldClientTest extends Specification {
    private static final Logger LOG = LoggerFactory.getLogger(ShieldClientTest.class)
    private static final boolean SHIELD_MOCKED = Boolean.valueOf(System.getProperty("shield.mocked"))
    private static final String SHIELD_URL = System.getProperty("shield.url")
    private static final String SHIELD_USERNAME = System.getProperty("shield.username")
    private static final String SHIELD_PASSWORD = System.getProperty("shield.password")
    private static final String SHIELD_API_KEY = System.getProperty("shield.api.key")
    private static final String SHIELD_TEST = "TEST-SHIELD-CLIENT"
    private static final String SHIELD_SYSTEM_TEST = "TEST-SYSTEM-SHIELD-CLIENT"
    private static final String SHIELD_AGENT_URL = "127.0.0.1:5444"
    private static ShieldConfig emptyShieldConfig = new ShieldConfig()
    private static ShieldRestClientFactory shieldRestClientFactory = new ShieldRestClientFactory([new ShieldRestClientv1(
            emptyShieldConfig)])

    private static BackupParameter backupParameter = backupParameter().
            storeName("default").
            retentionName("default").
            scheduleName("default").
            schedule("daily 3am").
            build()
    private ShieldTarget shieldTarget
    private ShieldClient sut

    @ClassRule
    private static WireMockRule shieldWireMock = new WireMockRule(options().
            withRootDirectory("src/test/resources/shield").
            port(8082))

    @Shared
    private String backupTaskUUID

    void setupSpec() {
        shieldWireMock.start()
        if (!SHIELD_MOCKED) {
            LOG.info("Start recording with shield wiremock targeting '{}'", SHIELD_URL)
            shieldWireMock.startRecording(recordSpec().
                    forTarget(SHIELD_URL).
                    extractBinaryBodiesOver(10240).
                    extractTextBodiesOver(256).
                    makeStubsPersistent(true))
        }
    }

    void setup() {
        shieldTarget = new TestShieldTarget()
        ShieldConfig shieldConfig = new ShieldConfig()
        shieldConfig.baseUrl = "http://localhost:8082"
        shieldConfig.username = SHIELD_USERNAME
        shieldConfig.password = SHIELD_PASSWORD
        shieldConfig.apiKey = SHIELD_API_KEY
        ShieldRestClientFactory shieldRestClientFactory = new ShieldRestClientFactory([new ShieldRestClientv1(
                shieldConfig)])
        sut = new ShieldClient(shieldConfig,
                               shieldRestClientFactory,
                               new BackupPersistenceService())
        LOG.info("Testing against {} with {}",
                 SHIELD_MOCKED ? "mocked shield" : "live shield",
                 shieldConfig.toString())
    }

    void waitUntilEndStateOrTimeout(String taskUuid, int timeoutInSeconds) {
        int intervallInSeconds = SHIELD_MOCKED ? 0 : 2
        LocalTime start = LocalTime.now()
        while (start.plusSeconds(timeoutInSeconds).isAfter(LocalTime.now())) {
            JobStatus result = sut.getJobStatus(taskUuid)
            if (result == JobStatus.SUCCESSFUL || result == JobStatus.FAILED) {
                return
            }
            Thread.sleep(intervallInSeconds * 1000)
        }
    }

    def cleanupSpec() {
        if (!SHIELD_MOCKED) {
            shieldWireMock.stopRecording()
        }
        shieldWireMock.stop()
    }

    class TestShieldTarget implements ShieldTarget {

        @Override
        String pluginName() {
            return "dummy"
        }

        @Override
        String endpointJson() {
            new JsonGenerator.Options().excludeNulls().build().toJson(
                    [data: SHIELD_TEST])
        }
    }

    @Unroll
    def "Create ShieldClient with false parameters because #message"() {
        when:
        ShieldClient shieldClient = new ShieldClient(config, restClientFactory, backupPersistenceService)

        then:
        shieldClient == null
        def exception = thrown(IllegalArgumentException.class)
        exception.message == message

        where:
        config            | restClientFactory       | backupPersistenceService       | message
        null              | shieldRestClientFactory | new BackupPersistenceService() | "Shield config cannot be null!"
        emptyShieldConfig | null                    | new BackupPersistenceService() | "Shield REST client factory cannot be null!"
        emptyShieldConfig | shieldRestClientFactory | null                           | "Backup persistence service cannot be null!"
    }

    def "register and run job"() {
        given:
        String jobName = SHIELD_TEST + "-JOB"
        String targetName = SHIELD_TEST + "-TARGET"

        when:
        String result = sut.registerAndRunJob(jobName, targetName, shieldTarget, backupParameter, SHIELD_AGENT_URL)
        backupTaskUUID = result
        LOG.info("Backup Task: " + result)

        then:
        Assert.notEmpty([result], "Result should show Task UUID of backup task")
    }

    def "should get task status in progress"() {
        when:
        JobStatus result = sut.getJobStatus(backupTaskUUID)

        then:
        result == JobStatus.RUNNING

        cleanup:
        waitUntilEndStateOrTimeout(backupTaskUUID, 5)
    }

    def "should get task status successful after wait"() {
        when:
        JobStatus result = sut.getJobStatus(backupTaskUUID)

        then:
        result == JobStatus.SUCCESSFUL
    }

    def "restore backup"() {
        when:
        String result = sut.restore(backupTaskUUID)
        LOG.info("Restore Task: " + result)

        then:
        Assert.notEmpty([result], "Result should show Task UUID of restore task")

        cleanup:
        waitUntilEndStateOrTimeout(result, 5)
    }

    def "delete jobs and backups"() {
        when:
        sut.deleteJobsAndBackups(SHIELD_TEST)

        then:
        noExceptionThrown()
    }

    def "register and run system backup"() {
        when:
        Collection<ServiceDetail> result = sut.registerAndRunSystemBackup(SHIELD_SYSTEM_TEST + "-JOB",
                                                                          SHIELD_SYSTEM_TEST + "-TARGET",
                                                                          shieldTarget,
                                                                          backupParameter,
                                                                          SHIELD_AGENT_URL)

        then:
        result.find {it.key == SHIELD_JOB_UUID.key}.value.length() > 0
        result.find {it.key == SHIELD_TARGET_UUID.key}.value.length() > 0
    }

    def "unregister system backup"() {
        when:
        sut.unregisterSystemBackup(SHIELD_SYSTEM_TEST + "-JOB", SHIELD_SYSTEM_TEST + "-TARGET")

        then:
        noExceptionThrown()
    }

    def "unregister should not fail when trying to delete unknown job or target"() {
        when:
        sut.unregisterSystemBackup("JOBDOESNOTEXIST", "TARGETDOESNOTEXIST")

        then:
        noExceptionThrown()
    }

    @Unroll
    def "register and run system backup should fail when given illegal arguments because #message"() {
        when:
        Collection<ServiceDetail> result = sut.registerAndRunSystemBackup(jobName,
                                                                          targetName,
                                                                          target,
                                                                          parameters,
                                                                          agentUrl)

        then:
        result == null
        def exception = thrown(IllegalArgumentException.class)
        exception.message == message

        where:
        jobName | targetName | target                 | parameters      | agentUrl         | message
        null    | "target"   | new TestShieldTarget() | backupParameter | SHIELD_AGENT_URL | "Job name cannot be empty!"
        ""      | "target"   | new TestShieldTarget() | backupParameter | SHIELD_AGENT_URL | "Job name cannot be empty!"
        "job"   | null       | new TestShieldTarget() | backupParameter | SHIELD_AGENT_URL | "Target name cannot be empty!"
        "job"   | ""         | new TestShieldTarget() | backupParameter | SHIELD_AGENT_URL | "Target name cannot be empty!"
        "job"   | "target"   | null                   | backupParameter | SHIELD_AGENT_URL | "ShieldTarget cannot be null!"
        "job"   | "target"   | new TestShieldTarget() | null            | SHIELD_AGENT_URL | "BackupParameter cannot be null!"
        "job"   | "target"   | new TestShieldTarget() | backupParameter | null             | "Shield agent URL cannot be empty!"
        "job"   | "target"   | new TestShieldTarget() | backupParameter | ""               | "Shield agent URL cannot be empty!"
    }

    @Unroll
    def "unregister system backup should fail when given illegal arguments because #message"() {
        when:
        sut.unregisterSystemBackup(jobName, targetName)

        then:
        def exception = thrown(IllegalArgumentException.class)
        exception.message == message

        where:
        jobName | targetName | message
        null    | "target"   | "Job name cannot be empty!"
        ""      | "target"   | "Job name cannot be empty!"
        "job"   | null       | "Target name cannot be empty!"
        "job"   | ""         | "Target name cannot be empty!"
    }

    def "restore backup should fail given an unknown taskUUID"() {
        when:
        String result = sut.restore("TASKDOESNOTEXIST")

        then:
        result == null
        thrown(ShieldResourceNotFoundException.class)
    }

    def "delete jobs and backups should not fail when given unknown job"() {
        when:
        sut.deleteJobsAndBackups("JOBDOESNOTEXIST")

        then:
        noExceptionThrown()
    }

    @Unroll
    def "delete jobs and backups should fail when given wrong parameters because #message"() {
        when:
        sut.deleteJobsAndBackups(jobName)

        then:
        def exception = thrown(IllegalArgumentException.class)
        exception.message == message

        where:
        jobName | message
        null    | "Service Instance GUID cannot be empty!"
        ""      | "Service Instance GUID cannot be empty!"
    }

    @Unroll
    def "get task status should fail because #message"() {
        when:
        JobStatus result = sut.getJobStatus(taskUUID)

        then:
        result == null
        def exception = thrown(Exception.class)
        exception.message == message

        where:
        taskUUID           | message
        null               | "Task UUID cannot be empty!"
        "TASKDOESNOTEXIST" | "Rest call to Shield failed with status:501 NOT_IMPLEMENTED"
    }

    @Unroll
    def "register and run system backup throws ServiceBrokerException when given 500 or 504 failure at #failingUrl"() {
        given:
        shieldWireMock.stubFor(WireMock.post(urlEqualTo((String) failingUrl)).willReturn(
                WireMock.aResponse().withStatus(500)
        ))
        when:
        Collection<ServiceDetail> result = sut.registerAndRunSystemBackup(SHIELD_SYSTEM_TEST + "-FAILURE-JOB",
                                                                          SHIELD_SYSTEM_TEST + "-FAILURE-TARGET",
                                                                          shieldTarget,
                                                                          backupParameter,
                                                                          SHIELD_AGENT_URL)

        then:
        result == null
        thrown(ServiceBrokerException)

        where:
        failingUrl << ["/v1/targets", "/v1/jobs"]
    }
}
