package com.swisscom.cf.broker.async.job

import com.swisscom.cf.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cf.broker.model.LastOperation

import java.util.concurrent.atomic.AtomicInteger

public class InProgressJob extends AbstractLastOperationJob {
    public static final AtomicInteger ExecutionCount = new AtomicInteger()

    @Override
    public JobResult handleJob(LastOperationJobContext context) {
        ExecutionCount.incrementAndGet()
        return new JobResult(status: LastOperation.Status.IN_PROGRESS)
    }
}