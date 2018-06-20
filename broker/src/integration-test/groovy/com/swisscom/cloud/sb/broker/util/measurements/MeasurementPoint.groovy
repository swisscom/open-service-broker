package com.swisscom.cloud.sb.broker.util.measurements

import org.influxdb.annotation.Column
import org.influxdb.annotation.Measurement

@Measurement(name="measurements")
class MeasurementPoint {
    @Column(name = "name")
    String name
    Class measurementPointClass
    double expectedReturnValue

    // Constructor is necessary for the InfluxDBResultMapper.toPOJO method
    MeasurementPoint() {
    }

    MeasurementPoint(String name, Class measurementPointClass) {
        this.name = name
        this.measurementPointClass = measurementPointClass
    }

    MeasurementPoint(String name, Class measurementPointClass, double expectedReturnValue) {
        this.name = name
        this.measurementPointClass = measurementPointClass
        this.expectedReturnValue = expectedReturnValue
    }
}
