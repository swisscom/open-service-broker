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

package com.swisscom.cloud.sb.broker.async.job

import com.swisscom.cloud.sb.broker.BaseSpecification
import com.swisscom.cloud.sb.broker.async.AsyncProvisioningService
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.provisioning.job.ProvisioningJobConfig
import com.swisscom.cloud.sb.broker.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.repository.ProvisionRequestRepository
import com.swisscom.cloud.sb.broker.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.util.DBTestUtil
import com.swisscom.cloud.sb.broker.util.ServiceLifeCycler
import com.swisscom.cloud.sb.broker.util.StringGenerator
import org.quartz.TriggerKey
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.IgnoreIf
import spock.lang.Shared

@IgnoreIf({ Boolean.valueOf(System.properties['spock.ignore.longRunning']) })
class JobManagerSpec extends BaseSpecification {
    @Shared
    private String id

    @Autowired
    JobManager jobManager

    @Autowired
    AsyncProvisioningService asyncProvisioningService

    public static Collection results = []

    @Shared
    ServiceLifeCycler serviceLifeCycler

    @Shared
    ServiceInstanceRepository serviceInstanceRepository

    @Shared
    DBTestUtil dbTestUtil

    @Autowired
    LastOperationRepository lastOperationRepository

    @Autowired
    ProvisionRequestRepository provisionRequestRepository
    @Shared
    boolean initialized
    @Shared
    private String serviceName = 'someServiceForJobQueueSpec'


    def setupSpec() {
        id = 'TEST_' + StringGenerator.randomUuid()
    }

    @Autowired
    void poorMansSetupSpec(DBTestUtil dbTestUtil,ServiceInstanceRepository serviceInstanceRepository) {
        if (!initialized) {
            this.dbTestUtil = dbTestUtil
            this.serviceInstanceRepository = serviceInstanceRepository

            dbTestUtil.createServiceInstance(serviceName, id)
            initialized = true
        }
    }

    def cleanupSpec() {
        serviceInstanceRepository.deleteByGuid(id)
    }

    def cleanup() {
        removeLastOperation(id)
        and:
        jobManager.dequeue(id)
        results = []
    }

    def "failing job is handled correctly"() {
        given:
        int executionIntervalInSeconds = 2

        when:
        asyncProvisioningService.scheduleProvision(new ProvisioningJobConfig(FailingJob.class, new ProvisionRequest(serviceInstanceGuid: id), executionIntervalInSeconds, 0.2))
        Thread.sleep((executionIntervalInSeconds * 1) * 1000)

        then:
        lastOperationRepository.findByGuid(id).status == LastOperation.Status.FAILED
        and:
        !jobManager.quartzSchedulerWithPersistence.getScheduler().checkExists(TriggerKey.triggerKey(id))
        and:
        FailingJob.ExecutionCount.get() == 1

        cleanup:
        def request = provisionRequestRepository.findByServiceInstanceGuid(id)
        if (request) {
            provisionRequestRepository.deleteByServiceInstanceGuid(id)
        }
    }

    def "job that can't complete in a configured timeframe is handled correctly"() {
        given:
        int executionIntervalInSeconds = 5
        double durationInMinutes = 0.3
        double totalDurationInSeconds = ((durationInMinutes * 60) * 1000)
        when:
        asyncProvisioningService.scheduleProvision(new ProvisioningJobConfig(InProgressJob.class, new ProvisionRequest(serviceInstanceGuid: id), executionIntervalInSeconds, durationInMinutes))
        Thread.sleep((totalDurationInSeconds as int) + executionIntervalInSeconds)

        then:
        lastOperationRepository.findByGuid(id).status == LastOperation.Status.FAILED
        and:
        !jobManager.quartzSchedulerWithPersistence.getScheduler().checkExists(TriggerKey.triggerKey(id))
        and:
        InProgressJob.ExecutionCount.get() <= (totalDurationInSeconds / executionIntervalInSeconds) + 1
    }

    def "successful job is handled correctly"() {
        given:
        int executionIntervalInSeconds = 2
        SuccessfulJob.ExecutionCount.set(0)
        when:
        asyncProvisioningService.scheduleProvision(new ProvisioningJobConfig(SuccessfulJob.class, new ProvisionRequest(serviceInstanceGuid: id), executionIntervalInSeconds, 1))
        Thread.sleep((executionIntervalInSeconds * 2) * 1000)

        then:
        lastOperationRepository.findByGuid(id).status == LastOperation.Status.SUCCESS
        and:
        !jobManager.quartzSchedulerWithPersistence.getScheduler().checkExists(TriggerKey.triggerKey(id))
        and:
        SuccessfulJob.ExecutionCount.get() == 1
    }

    def "exception throwing job is handled correctly"() {
        given:
        int executionIntervalInSeconds = 2
        when:
        asyncProvisioningService.scheduleProvision(new ProvisioningJobConfig(ExceptionThrowingJob.class, new ProvisionRequest(serviceInstanceGuid: id), executionIntervalInSeconds, 1))
        lastOperationRepository.findByGuid(id).status == LastOperation.Status.IN_PROGRESS
        then:
        Thread.sleep((executionIntervalInSeconds * 2) * 1000)
        lastOperationRepository.findByGuid(id).status == LastOperation.Status.FAILED
        lastOperationRepository.findByGuid(id).description == "Something terrible happened"
        and:
        !jobManager.quartzSchedulerWithPersistence.getScheduler().checkExists(TriggerKey.triggerKey(id))
        and:
        ExceptionThrowingJob.ExecutionCount.get() == 1
    }

    private removeLastOperation(String id) {
        lastOperationRepository.deleteByGuid(id)
    }
}
