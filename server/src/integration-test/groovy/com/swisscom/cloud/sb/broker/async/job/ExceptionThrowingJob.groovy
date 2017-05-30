package com.swisscom.cloud.sb.broker.async.job

import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncOperationResult

import java.util.concurrent.atomic.AtomicInteger

public class ExceptionThrowingJob extends AbstractLastOperationJob {
    public static final AtomicInteger ExecutionCount = new AtomicInteger()

    @Override
    public AsyncOperationResult handleJob(LastOperationJobContext context) {
        ExecutionCount.incrementAndGet()
        throw new RuntimeException('Something terrible happened')
    }
}