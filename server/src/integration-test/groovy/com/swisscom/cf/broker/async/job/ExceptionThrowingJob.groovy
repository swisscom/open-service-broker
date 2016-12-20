package com.swisscom.cf.broker.async.job

import com.swisscom.cf.broker.provisioning.async.AsyncOperationResult
import com.swisscom.cf.broker.provisioning.lastoperation.LastOperationJobContext

import java.util.concurrent.atomic.AtomicInteger

public class ExceptionThrowingJob extends AbstractLastOperationJob {
    public static final AtomicInteger ExecutionCount = new AtomicInteger()

    @Override
    public AsyncOperationResult handleJob(LastOperationJobContext context) {
        ExecutionCount.incrementAndGet()
        throw new RuntimeException('Something terrible happened')
    }
}