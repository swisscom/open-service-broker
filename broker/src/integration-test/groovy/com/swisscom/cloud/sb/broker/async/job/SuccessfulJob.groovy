package com.swisscom.cloud.sb.broker.async.job

import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncOperationResult
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext

import java.util.concurrent.atomic.AtomicInteger

public class SuccessfulJob extends AbstractLastOperationJob {
    public static final AtomicInteger ExecutionCount  = new AtomicInteger()

    @Override
    public AsyncOperationResult handleJob(LastOperationJobContext context) {
        ExecutionCount.incrementAndGet()
        return new AsyncOperationResult(status: LastOperation.Status.SUCCESS)
    }
}