package com.swisscom.cloud.sb.broker.metrics.measurements

import org.influxdb.annotation.Column
import org.influxdb.annotation.Measurement

@Measurement(name="measurements")
class MeasurementPoint {
    @Column(name = "name")
    String name

    // Constructor is necessary for the InfluxDBResultMapper.toPOJO method
    MeasurementPoint() {
    }

    MeasurementPoint(String name) {
        this.name = name
    }
}
