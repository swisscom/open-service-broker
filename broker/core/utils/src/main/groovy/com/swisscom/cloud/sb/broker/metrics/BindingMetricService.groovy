package com.swisscom.cloud.sb.broker.metrics

interface BindingMetricService {
    void notifyBinding(String serviceInstanceUuid, boolean bindingSucceeded)
}