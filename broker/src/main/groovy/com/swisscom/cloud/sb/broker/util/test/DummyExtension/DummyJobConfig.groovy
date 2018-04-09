package com.swisscom.cloud.sb.broker.util.test.DummyExtension

import com.swisscom.cloud.sb.broker.async.job.AbstractJob
import com.swisscom.cloud.sb.broker.async.job.JobConfig
import groovy.transform.CompileStatic

@CompileStatic
class DummyJobConfig extends JobConfig{

    DummyJobConfig(Class<? extends AbstractJob> jobClass, String guid, int retryIntervalInSeconds, double maxRetryDurationInMinutes) {
        super(jobClass, guid, retryIntervalInSeconds, maxRetryDurationInMinutes)
    }
}