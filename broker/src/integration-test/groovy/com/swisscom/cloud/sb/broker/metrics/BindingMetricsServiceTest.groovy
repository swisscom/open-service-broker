package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.BaseSpecification
import org.aspectj.lang.annotation.control.CodeGenerationHint
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory
import org.influxdb.annotation.Column
import org.influxdb.annotation.Measurement
import org.influxdb.dto.Point
import org.influxdb.dto.Query
import org.influxdb.dto.QueryResult
import org.influxdb.impl.InfluxDBResultMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jmx.support.MetricType
import spock.lang.Ignore

@Ignore
class BindingMetricsServiceTest extends BaseSpecification {

    @Autowired
    BindingMetricsService bindingMetricsService

    @Measurement(name = "binding_total_total")
    class BindingTotalTotalPoint {
        @Column(name = "metric_type")
        MetricType metricType

        @Column(name = "value")
        Long value
    }

    def "binding metrics for empty database"(){
        when:
        bindingMetricsService.addMetricsToMeterRegistry(bindingMetricsService.meterRegistry, bindingMetricsService.serviceBindingRepository)

        then:
        InfluxDB influxDB = InfluxDBFactory("http://0.0.0.0:8086", null, null)
        influxDB.setDatabase("mydb")
        QueryResult queryResult = influxDB.query("Select * from binding_total_total")
        InfluxDBResultMapper influxDBResultMapper = new InfluxDBResultMapper()
        List<BindingTotalTotalPoint> bindingTotalTotalPointList = influxDBResultMapper.toPOJO(queryResult, BindingTotalTotalPoint.class)

        assert(bindingTotalTotalPointList.get(0).getValue() == 0)
    }
}
