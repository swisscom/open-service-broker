package com.swisscom.cloud.sb.broker.metrics

import groovy.util.logging.Slf4j
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.Point
import org.springframework.boot.actuate.autoconfigure.ExportMetricWriter
import org.springframework.boot.actuate.endpoint.MetricsEndpoint
import org.springframework.boot.actuate.endpoint.MetricsEndpointMetricReader
import org.springframework.boot.actuate.metrics.Metric
import org.springframework.boot.actuate.metrics.writer.Delta
import org.springframework.boot.actuate.metrics.writer.MetricWriter
import org.springframework.context.annotation.Bean

import java.util.concurrent.TimeUnit

@Slf4j
class InfluxDBConnector {

    @Bean
    @ExportMetricWriter
    MetricWriter influxMetricsWriter() {
        InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:8086", "root", "root")
        //influxDB.createDatabase(dbName);
        String dbName = "mydb";
        influxDB.setDatabase(dbName);
        //influxDB.setRetentionPolicy("one_day");
        influxDB.enableBatch(10, 1000, TimeUnit.MILLISECONDS);

        return new MetricWriter() {

            @Override
            public void set(Metric<?> value) {
                Point point = Point.measurement(value.getName()).time(value.getTimestamp().getTime(), TimeUnit.MILLISECONDS)
                        .addField("value", value.getValue()).build();
                influxDB.write(point);
                log.info("write(" + value.getName() + "): " + value.getValue());
            }

            @Override
            void increment(Delta<?> delta) {

            }

            @Override
            void reset(String metricName) {

            }
        }
    }

    @Bean
    public MetricsEndpointMetricReader metricsEndpointMetricReader(final MetricsEndpoint metricsEndpoint) {
        return new MetricsEndpointMetricReader(metricsEndpoint);
    }
}