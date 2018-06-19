package com.swisscom.cloud.sb.broker.metrics.measurements

import org.influxdb.annotation.Column
import org.influxdb.annotation.Measurement

import java.time.Instant

@Measurement(name = "bindingRequest_service_fail_service")
class BindingRequestServiceFailServicePoint {
    @Column(name = "time")
    Instant time
    @Column(name = "value")
    double value
}
