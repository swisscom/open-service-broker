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

import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.swisscom.cloud.sb.broker.async.job.JobStatus
import com.swisscom.cloud.sb.broker.backup.BackupPersistenceService
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import groovy.json.JsonGenerator
import org.joda.time.LocalTime
import org.junit.ClassRule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.util.Assert
import spock.lang.Specification
import spock.lang.Unroll
import java.time.Duration

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import static com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER
import static com.github.tomakehurst.wiremock.http.Fault.MALFORMED_RESPONSE_CHUNK
import static com.swisscom.cloud.sb.broker.backup.shield.BackupParameter.backupParameter
import static com.swisscom.cloud.sb.broker.util.servicedetail.ShieldServiceDetailKey.SHIELD_JOB_UUID
import static com.swisscom.cloud.sb.broker.util.servicedetail.ShieldServiceDetailKey.SHIELD_TARGET_UUID

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
        shieldConfig.maxNumberOfApiRetries = 3
        shieldConfig.waitBetweenApiRetries = Duration.ofMillis(10)
        sut = new ShieldClient(shieldConfig,
                               new BackupPersistenceService())
        LOG.info("Testing against {} with {}",
                 SHIELD_MOCKED ? "mocked shield" : "live shield",
                 shieldConfig.toString())
        shieldWireMock.resetScenarios()
    }

    JobStatus waitUntilEndStateOrTimeout(String taskUuid, int timeoutInSeconds) {
        Assert.hasText(taskUuid, "Task UUID must be set!")
        Assert.isTrue(timeoutInSeconds > 0, "Timeout in seconds must be positive!")
        int intervallInSeconds = SHIELD_MOCKED ? 0 : 2
        LocalTime start = LocalTime.now()
        JobStatus result
        while (start.plusSeconds(timeoutInSeconds).isAfter(LocalTime.now())) {
            result = sut.getJobStatus(taskUuid)
            if (result == JobStatus.SUCCESSFUL || result == JobStatus.FAILED) {
                return result
            }
            Thread.sleep(intervallInSeconds * 1000)
        }
        if (result == null) {
            throw new IllegalStateException("No JobStatus received")
        }
        return result
    }

    String setupBackup(String jobName, String targetName) {
        String backupTaskUUID = sut.registerAndRunJob(jobName,
                                                      targetName,
                                                      shieldTarget,
                                                      backupParameter,
                                                      SHIELD_AGENT_URL)
        LOG.info("Backup Task: " + backupTaskUUID)
        JobStatus status = waitUntilEndStateOrTimeout(backupTaskUUID, 10)
        if (status != JobStatus.SUCCESSFUL) {
            throw new IllegalStateException("Setup Backup failed!")
        }
        return backupTaskUUID
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
                    [data: SHIELD_TEST, file: 'TEST'])
        }
    }

    @Unroll
    def "Create ShieldClient with false parameters because #message"() {
        when:
        ShieldClient shieldClient = new ShieldClient(config, backupPersistenceService)

        then:
        shieldClient == null
        def exception = thrown(IllegalArgumentException.class)
        exception.message == message

        where:
        config            | backupPersistenceService       | message
        null              | new BackupPersistenceService() | "Shield config cannot be null!"
        emptyShieldConfig | null                           | "Backup persistence service cannot be null!"
    }

    def "register and run job"() {
        given:
        String jobName = SHIELD_TEST + "REGISTER-AND-RUN-JOB"
        String targetName = SHIELD_TEST + "REGISTER-AND-RUN-TARGET"

        when:
        String backupTaskUUID = sut.registerAndRunJob(jobName,
                                                      targetName,
                                                      shieldTarget,
                                                      backupParameter,
                                                      SHIELD_AGENT_URL)
        LOG.info("Backup Task: " + backupTaskUUID)
        JobStatus firstStatus = sut.getJobStatus(backupTaskUUID)
        JobStatus lastStatus = waitUntilEndStateOrTimeout(backupTaskUUID, 10)

        then:
        Assert.notEmpty([backupTaskUUID], "Result should show Task UUID of backup task")
        firstStatus == JobStatus.RUNNING
        lastStatus == JobStatus.SUCCESSFUL

        cleanup:
        sut.deleteJobsAndBackups(SHIELD_TEST + "REGISTER-AND-RUN")
    }

    def "restore backup"() {
        given:
        String backupTaskUuid = setupBackup(SHIELD_TEST + "RESTORE-JOB", SHIELD_TEST + "RESTORE-TARGET")

        when:
        String result = sut.restore(backupTaskUuid)
        LOG.info("Restore Task: " + result)
        JobStatus status = waitUntilEndStateOrTimeout(result, 10)


        then:
        Assert.notEmpty([result], "Result should show Task UUID of restore task")
        status == JobStatus.SUCCESSFUL

        cleanup:
        sut.deleteJobsAndBackups(SHIELD_TEST + "RESTORE")
    }

    def "delete jobs and backups"() {
        given:
        setupBackup(SHIELD_TEST + "DELETE-JOB", SHIELD_TEST + "DELETE-TARGET")

        when:
        BackupDeregisterInformation result = sut.deleteJobsAndBackups(SHIELD_TEST)

        then:
        result.deletedSomething()
        result.getNumberOfDeletedJobs() == 1
        result.getNumberOfDeletedTargets() == 1
        result.getDeletedJobs().first() == SHIELD_TEST + "DELETE-JOB"
        result.getDeletedTargets().first() == SHIELD_TEST + "DELETE-TARGET"
    }

    def "register and run system backup"() {
        when:
        Collection<ServiceDetail> result = sut.registerAndRunSystemBackup(SHIELD_SYSTEM_TEST + "-REGISTER-JOB",
                                                                          SHIELD_SYSTEM_TEST + "-REGISTER-TARGET",
                                                                          shieldTarget,
                                                                          backupParameter,
                                                                          SHIELD_AGENT_URL)

        then:
        result.find {it.key == SHIELD_JOB_UUID.key}.value.length() > 0
        result.find {it.key == SHIELD_TARGET_UUID.key}.value.length() > 0

        cleanup:
        sut.unregisterSystemBackup(SHIELD_SYSTEM_TEST + "-REGISTER-JOB", SHIELD_SYSTEM_TEST + "-REGISTER-TARGET")
    }

    def "unregister system backup"() {
        given:
        sut.registerAndRunSystemBackup(SHIELD_SYSTEM_TEST + "-UNREGISTER-JOB",
                                       SHIELD_SYSTEM_TEST + "-UNREGISTER-TARGET",
                                       shieldTarget,
                                       backupParameter,
                                       SHIELD_AGENT_URL)

        when:
        sut.unregisterSystemBackup(SHIELD_SYSTEM_TEST + "-UNREGISTER-JOB", SHIELD_SYSTEM_TEST + "-UNREGISTER-TARGET")

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

    def "restore backup should fail given an invalid taskUUID"() {
        when:
        String result = sut.restore("TASKDOESNOTEXIST")

        then:
        result == null
        thrown(IllegalArgumentException.class)
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
        taskUUID                               | message
        null                                   | "Task UUID cannot be null!"
        "TASKDOESNOTEXIST"                     | "Invalid UUID string: TASKDOESNOTEXIST"
        "3dc1d63c-3713-40c8-9af6-8f1ee96dd674" | "Failed to get task"
    }

    @Unroll
    def "register and run system backup throws ServiceBrokerException when Shield responds with #reason"() {
        given:
        shieldWireMock.stubFor(post(urlEqualTo((String) "/v1/targets")).willReturn(failure))
        when:
        Collection<ServiceDetail> result = sut.registerAndRunSystemBackup(SHIELD_SYSTEM_TEST + "-FAILURE-JOB",
                                                                          SHIELD_SYSTEM_TEST + "-FAILURE-TARGET",
                                                                          shieldTarget,
                                                                          backupParameter,
                                                                          SHIELD_AGENT_URL)

        then:
        result == null
        def ex = thrown(ShieldApiException)
        ex.toString().startsWith(message)

        where:
        failure                                         | message                                                                                                                                                                                                 | reason
        aResponse().
                withStatus(500)                         | "ShieldApiException[500 - Failed to create target: 500 Server Error: [no body]]"                                                                                                                                   | "HTTP Status Code 500"
        aResponse().
                withFault(MALFORMED_RESPONSE_CHUNK)     | "ShieldApiException[- - Failed to create target: I/O error on POST request for \"http://localhost:8082/v1/targets\": "         | "OK Status Code withGarbage body"
        aResponse().
                withFault(CONNECTION_RESET_BY_PEER)     | "ShieldApiException[- - Failed to create target: I/O error on POST request for \"http://localhost:8082/v1/targets\": Connection reset; nested exception is java.net.SocketException: Connection reset]" | "Connection reset by peer"


    }
}
