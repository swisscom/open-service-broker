package com.swisscom.cloud.sb.broker.metrics


import org.slf4j.Logger
import org.slf4j.LoggerFactory

class NoOpLastOperationMetricService extends NoOpPlanMetricService implements LastOperationMetricService {
    private final static Logger LOGGER = LoggerFactory.getLogger(NoOpLastOperationMetricService.class)

    @Override
    void notifySucceeded(String planGuid) {
        LOGGER.debug("notifySucceeded({})", planGuid)
    }

    @Override
    void notifyFailedWithTimeout(String planGuid) {
        LOGGER.debug("notifyFailedWithTimeout({})", planGuid)
    }

    @Override
    void notifyFailedWithException(String planGuid) {
        LOGGER.debug("notifyFailedWithException({})", planGuid)
    }

    @Override
    void notifyFailedByServiceProvider(String planGuid) {
        LOGGER.debug("notifyFailedByServiceProvider({})", planGuid)
    }
}
