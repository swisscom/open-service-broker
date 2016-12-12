package com.swisscom.cf.broker.async.job

import groovy.transform.CompileStatic
import org.quartz.DisallowConcurrentExecution
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@CompileStatic
@DisallowConcurrentExecution
@Transactional
abstract class AbstractJob implements Job {

    @Autowired
    private JobManager jobQueue

    protected boolean dequeue(String id) {
        return jobQueue.dequeue(id)
    }

    public static boolean isExecutedForLastTime(JobExecutionContext jobExecutionContext) {
        return jobExecutionContext.trigger.nextFireTime == null
    }

    public static String getJobId(JobExecutionContext jobExecutionContext) {
        return jobExecutionContext.trigger.getKey().getName()
    }
}
