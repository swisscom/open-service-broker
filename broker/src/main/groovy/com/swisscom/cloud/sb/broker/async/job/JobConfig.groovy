package com.swisscom.cloud.sb.broker.async.job

import com.google.common.base.Preconditions
import groovy.transform.CompileStatic

@CompileStatic
abstract class JobConfig {
    public static final int RETRY_INTERVAL_IN_SECONDS = 15
    public static final double MAX_RETRY_DURATION_IN_MINUTES = 30

    final Class<? extends AbstractJob> jobClass
    final String guid
    final int retryIntervalInSeconds
    final double maxRetryDurationInMinutes

    JobConfig(Class<? extends AbstractJob> jobClass, String guid,
              int retryIntervalInSeconds = RETRY_INTERVAL_IN_SECONDS,
              double maxRetryDurationInMinutes = MAX_RETRY_DURATION_IN_MINUTES) {
        Preconditions.checkNotNull(guid)
        Preconditions.checkArgument(retryIntervalInSeconds > 0, "retryIntervalInSeconds should be >0")
        Preconditions.checkArgument(maxRetryDurationInMinutes > 0, "maxRetryDurationInMinutes should be >0")

        this.jobClass = jobClass
        this.guid = guid
        this.retryIntervalInSeconds = retryIntervalInSeconds
        this.maxRetryDurationInMinutes = maxRetryDurationInMinutes
    }

    @Override
    String toString() {
        return "JobConfig{" +
                "jobClass=" + jobClass +
                ", guid='" + guid + '\'' +
                ", retryIntervalInSeconds=" + retryIntervalInSeconds +
                ", maxRetryDurationInMinutes=" + maxRetryDurationInMinutes +
                "}"
    }
}
