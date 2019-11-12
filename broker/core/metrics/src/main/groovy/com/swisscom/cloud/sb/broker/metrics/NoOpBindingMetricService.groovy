package com.swisscom.cloud.sb.broker.metrics

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class NoOpBindingMetricService implements BindingMetricService {
    private final static Logger LOGGER = LoggerFactory.getLogger(NoOpBindingMetricService.class)

    @Override
    void notifyBinding(String serviceInstanceUuid, boolean bindingSucceeded) {
        LOGGER.debug("notifyBinding({}, {})", serviceInstanceUuid, bindingSucceeded)
    }
}
