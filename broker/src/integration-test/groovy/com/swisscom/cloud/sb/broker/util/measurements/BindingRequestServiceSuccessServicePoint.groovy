package com.swisscom.cloud.sb.broker.util.measurements

import org.influxdb.annotation.Column
import org.influxdb.annotation.Measurement

import java.time.Instant

@Measurement(name = "bindingRequest_service_success_service1Name")
class BindingRequestServiceSuccessServicePoint {
    @Column(name = "time")
    Instant time
    @Column(name = "value")
    double value
}