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

package com.swisscom.cloud.sb.broker.backup

import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomakehurst.wiremock.stubbing.Scenario
import com.swisscom.cloud.sb.broker.model.Parameter
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.provisioning.ProvisioningPersistenceService
import org.junit.ClassRule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification
import spock.lang.Stepwise

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import static org.junit.Assert.assertEquals

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("default,test,secrets")
@Stepwise
class SystemBackupProviderTest extends Specification {
    private static final Logger LOG = LoggerFactory.getLogger(SystemBackupProviderTest.class)
    private static final String SERVICE_INSTANCE_ID = "44651d63-b7c0-4f20-86bb-efef081a99ca"
    private static final boolean SHIELD_MOCKED = Boolean.valueOf(System.getProperty("shield.mocked"))
    private static final String SHIELD_LIVE_TARGET_URL = System.getProperty("shield.live.target.url")

    @Autowired
    DummySystemBackupProvider backupProvider

    @Autowired
    BackupPersistenceService backupPersistenceService

    @ClassRule
    public static WireMockRule shieldWireMock

    void setupSpec() {
        if (!SHIELD_MOCKED) {
            shieldWireMock = new WireMockRule(options().
                    withRootDirectory("src/integration-test/resources/shield").
                    port(8082))
            LOG.info("Start recording with shield wiremock targeting '{}'", SHIELD_LIVE_TARGET_URL)
            shieldWireMock.start()
            shieldWireMock.startRecording(recordSpec().
                    forTarget(SHIELD_LIVE_TARGET_URL).
                    extractBinaryBodiesOver(10240).
                    extractTextBodiesOver(256).
                    makeStubsPersistent(true))
        } else {
            shieldWireMock = new WireMockRule(options().
                    usingFilesUnderClasspath("shield").
                    port(8082))
            shieldWireMock.start()
        }
    }

    void setup() {
        Set<Parameter> parameters = [new Parameter(name: "BACKUP_SCHEDULE", value: "default"),
                                     new Parameter(name: "BACKUP_SCHEDULE_NAME", value: "default"),
                                     new Parameter(name: "BACKUP_POLICY_NAME", value: "default"),
                                     new Parameter(name: "BACKUP_STORAGE_NAME", value: "default")]
        Plan plan = new Plan(parameters: parameters)
        backupProvider.provisioningPersistenceService = new ProvisioningPersistenceService() {
            ServiceInstance getServiceInstance(String guid) {
                return new ServiceInstance(guid: SERVICE_INSTANCE_ID, plan: plan)
            }
        }

        LOG.info("Testing against {}", SHIELD_MOCKED ? "mocked shield" : "live shield")
    }

    void cleanupSpec() {
        if (!SHIELD_MOCKED) {
            shieldWireMock.stopRecording()
        }
        shieldWireMock.stop()
    }

    void "register and run system backup with two 500 responses from shield api should succeed"() {
        given:
        shieldWireMock.stubFor(post(urlEqualTo("/v1/targets"))
                                       .inScenario("flaky shield api")
                                       .whenScenarioStateIs(Scenario.STARTED)
                                       .willReturn(aResponse().withStatus(500))
                                       .willSetStateTo("Failed targets once"))
        shieldWireMock.stubFor(post(urlEqualTo("/v1/jobs"))
                                       .inScenario("flaky shield api")
                                       .whenScenarioStateIs("Failed targets once")
                                       .willReturn(aResponse().withStatus(500))
                                       .willSetStateTo("Failed jobs once"))
        when:
        Collection<ServiceDetail> status = backupProvider.configureSystemBackup(SERVICE_INSTANCE_ID)
        then:
        assertEquals(status.size(), 2)
    }

    void "delete system backup with two 500 responses from shield api should succeed"() {
        given:
        shieldWireMock.stubFor(delete(urlMatching("/v1/job/([a-f0-9]{8}(\\-[a-f0-9]{4}){4}[a-f0-9]{8})"))
                                       .inScenario("flaky delete shield api")
                                       .whenScenarioStateIs(Scenario.STARTED)
                                       .willReturn(aResponse().withStatus(500))
                                       .willSetStateTo("Failed job once"))
        shieldWireMock.stubFor(delete(urlMatching("/v1/target/([a-f0-9]{8}(\\-[a-f0-9]{4}){4}[a-f0-9]{8})"))
                                       .inScenario("flaky delete shield api")
                                       .whenScenarioStateIs("Failed job once")
                                       .willReturn(aResponse().withStatus(500))
                                       .willSetStateTo("Failed target once"))
        when:
        backupProvider.unregisterSystemBackupOnShield(SERVICE_INSTANCE_ID)
        then:
        noExceptionThrown()
    }
}
