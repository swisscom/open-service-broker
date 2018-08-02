package com.swisscom.cloud.sb.broker.util.measurements

import org.influxdb.annotation.Column
import org.influxdb.annotation.Measurement

import java.time.Instant

@Measurement(name = "lifecycleTime_service_total_service1Name")
class LifecycleTimeServiceTotalServicePoint {
    @Column(name = "time")
    Instant time
    @Column(name = "value")
    double value
}
