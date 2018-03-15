package com.swisscom.cloud.sb.broker.util.test.DummyExtension

import com.swisscom.cloud.sb.broker.async.job.AbstractJob

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.quartz.JobExecutionContext

@CompileStatic
@Slf4j
class DummyJob extends AbstractJob {

    void execute(JobExecutionContext context){
        log.info("unlocking user")
        dequeue(getJobId(context))
    }
}
