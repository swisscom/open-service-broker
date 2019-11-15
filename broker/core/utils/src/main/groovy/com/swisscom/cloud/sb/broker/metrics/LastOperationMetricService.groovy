package com.swisscom.cloud.sb.broker.metrics

interface LastOperationMetricService extends PlanMetricService {
    void notifySucceeded(String planGuid);

    void notifyFailedWithTimeout(String planGuid);

    void notifyFailedWithException(String planGuid);

    void notifyFailedByServiceProvider(String planGuid);
}
