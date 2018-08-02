package com.swisscom.cloud.sb.broker.util.measurements

import org.influxdb.annotation.Column
import org.influxdb.annotation.Measurement

import java.time.Instant

@Measurement(name = "binding_total_total")
class BindingTotalTotalPoint {

    @Column(name = "time")
    Instant time
    @Column(name = "value")
    double value
}
