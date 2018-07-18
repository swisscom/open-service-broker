package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.config.Config
import groovy.transform.CompileStatic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@CompileStatic
@Configuration
@ConfigurationProperties(prefix = 'management.metrics.export.influx')
class ServiceBrokerMetricsConfig implements Config{
    String uri
    String step
    String userName
    String password
    String db

    String env
}
