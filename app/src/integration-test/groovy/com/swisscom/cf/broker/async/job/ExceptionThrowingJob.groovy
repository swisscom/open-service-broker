package com.swisscom.cf.broker.async.job

import com.swisscom.cf.broker.async.lastoperation.LastOperationJobContext

import java.util.concurrent.atomic.AtomicInteger

public class ExceptionThrowingJob extends AbstractLastOperationJob {
    public static final AtomicInteger ExecutionCount = new AtomicInteger()

    @Override
    public JobResult handleJob(LastOperationJobContext context) {
        ExecutionCount.incrementAndGet()
        throw new RuntimeException('Something terrible happened')
    }
}