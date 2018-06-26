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

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.quartz.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@Component
@CompileStatic
@Slf4j
class JobManager {
    @Autowired
    @Qualifier('quartzSchedulerWithPersistence')
    SchedulerFactoryBean quartzSchedulerWithPersistence

    @Autowired
    @Qualifier('quartzScheduler')
    SchedulerFactoryBean quartzScheduler

    @PostConstruct
    private void init() {
        quartzSchedulerWithPersistence.getScheduler().start()
    }

    def queue(JobConfig jobConfig) {
        log.info("Queueing job: ${jobConfig}")

        String jobId = jobConfig.guid

        def jobDetail = JobBuilder.newJob(jobConfig.jobClass)
                .withIdentity(jobId)
                .requestRecovery()
                .build()

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobConfig.guid)
                .forJob(jobId)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(jobConfig.retryIntervalInSeconds)
                .withRepeatCount(calculateMaxRepeatCount(jobConfig.maxRetryDurationInMinutes, jobConfig.retryIntervalInSeconds))
                .withMisfireHandlingInstructionNextWithExistingCount())
                .startNow()
                .build()

        log.debug("Inserting job detail and trigger with id: ${jobConfig.guid}")

        quartzSchedulerWithPersistence.getScheduler().scheduleJob(jobDetail, new HashSet<Trigger>([trigger]), true)
    }

    private static int calculateMaxRepeatCount(double maxRetryDurationInMinutes, int retryIntervalInSeconds) {
        (maxRetryDurationInMinutes * 60 / retryIntervalInSeconds) as int
    }

    def dequeue(String guid) {
        quartzSchedulerWithPersistence.getScheduler().unscheduleJob(TriggerBuilder.newTrigger().withIdentity(guid).build().key)
    }
}
