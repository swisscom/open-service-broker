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
import com.swisscom.cloud.sb.broker.backup.shield.restClient.ShieldRestClient
import com.swisscom.cloud.sb.broker.backup.shield.restClient.ShieldRestClientFactory
import com.swisscom.cloud.sb.broker.backup.shield.restClient.ShieldRestClientv1
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import groovy.json.JsonGenerator
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
import static com.google.common.base.Strings.isNullOrEmpty
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

    private ShieldTarget shieldTarget
    private BackupParameter backupParameter
    private String shieldAgentUrl

    private ShieldRestClient restClient
    private ShieldClient sut

    @ClassRule
    public static WireMockRule shieldWireMock = new WireMockRule(options().
            withRootDirectory("src/test/resources/shield").
            port(8082))

    @Shared
    String backupTaskUUID

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
        backupParameter = new BackupParameter(storeName: "default",
                                              retentionName: "default",
                                              scheduleName: "default",
                                              schedule: "daily 3am",
                                              agent: SHIELD_AGENT_URL)
        shieldAgentUrl = SHIELD_AGENT_URL
        ShieldConfig shieldConfig = new ShieldConfig()
        shieldConfig.baseUrl = "http://localhost:8082"
        shieldConfig.username = SHIELD_USERNAME
        shieldConfig.password = SHIELD_PASSWORD
        shieldConfig.apiKey = SHIELD_API_KEY
        restClient = new ShieldRestClientv1(shieldConfig, new RestTemplateBuilder())
        sut = new ShieldClient(shieldConfig,
                               new ShieldRestClientFactory([new ShieldRestClientv1(shieldConfig,
                                                                                   new RestTemplateBuilder())]),
                               new BackupPersistenceService())
        LOG.info("Testing against {} and with URL '{}' with username '{}' and password '{}' and api key '{}'",
                 SHIELD_MOCKED ? "mocked shield" : "live shield",
                 shieldConfig.getBaseUrl(),
                 shieldConfig.getUsername(),
                 isNullOrEmpty(shieldConfig.getPassword()) ? " NO PASSWORD PROVIDED" : "<CONFIDENTIAL>",
                 isNullOrEmpty(shieldConfig.getApiKey()) ? " NO API KEY PROVIDED" : "<CONFIDENTIAL>")
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

    def "register and run job"() {
        given:
        String jobName = SHIELD_TEST + "-JOB"
        String targetName = SHIELD_TEST + "-TARGET"

        when:
        String result = sut.registerAndRunJob(jobName, targetName, shieldTarget, backupParameter, shieldAgentUrl)
        backupTaskUUID = result
        LOG.info("Backup Task: " + result)

        then:
        Assert.notEmpty([result], "Result should show Task UUID of backup task")
        noExceptionThrown()
    }

    def "should get task status in progress"() {
        when:
        JobStatus result = sut.getJobStatus(backupTaskUUID)

        then:
        result == JobStatus.RUNNING
    }

    def "should get task status successful after wait"() {
        given:
        if (!SHIELD_MOCKED) {
            sleep(2000)
        }

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
    }

    def "delete jobs and backups"() {
        given:
        if (!SHIELD_MOCKED) {
            sleep(2000)
        }
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
                                                                          shieldAgentUrl)

        then:
        result.find {it.key == SHIELD_JOB_UUID.key}.value.length() > 0
        result.find {it.key == SHIELD_TARGET_UUID.key}.value.length() > 0
    }

    def "unregister system backup"() {
        given:
        if (!SHIELD_MOCKED) {
            sleep(2000)
        }

        when:
        sut.unregisterSystemBackup(SHIELD_SYSTEM_TEST + "-JOB", SHIELD_SYSTEM_TEST + "-TARGET")

        then:
        noExceptionThrown()
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
                                                                          shieldAgentUrl)

        then:
        result == null
        thrown(ServiceBrokerException)

        where:
        failingUrl << ["/v1/targets", "/v1/jobs"]
    }
}
