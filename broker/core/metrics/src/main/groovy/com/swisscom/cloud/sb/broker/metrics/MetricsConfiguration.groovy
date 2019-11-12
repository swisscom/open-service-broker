package com.swisscom.cloud.sb.broker.metrics


import io.micrometer.core.instrument.MeterRegistry
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MetricsConfiguration {

    @Bean
    @ConditionalOnMissingBean
    List<PlanMetricService> getNoOpPlanMetricServices() {
        return [new NoOpPlanMetricService()]
    }

    @Bean
    @ConditionalOnMissingBean
    BindingMetricService getNoOpBindingMetricServices() {
        return new NoOpBindingMetricService()
    }

    @Bean
    @ConditionalOnMissingBean
    LastOperationMetricService getNoOpLastOperationMetricServices() {
        return new NoOpLastOperationMetricService()
    }

    @Bean
    @ConditionalOnProperty(name = "management.metrics.export.influx.enabled", havingValue = "true")
    List<PlanMetricService> getInfluxMetricServices(
            MeterRegistry meterRegistry,
            MetricsCache metricsCache,
            ServiceBrokerMetricsConfig serviceBrokerMetricsConfig,
            BindingMetricService bindingMetricService,
            LastOperationMetricService lastOperationMetricService) {
        return [
                bindingMetricService,
                lastOperationMetricService,
                new LifecycleTimeMetricsService(
                        meterRegistry,
                        metricsCache,
                        serviceBrokerMetricsConfig),
                new ServiceInstanceMetricsService(
                        meterRegistry,
                        metricsCache,
                        serviceBrokerMetricsConfig)]
    }

    @Bean
    @ConditionalOnProperty(name = "management.metrics.export.influx.enabled", havingValue = "true")
    BindingMetricService getBindingMetricsService(
                    MeterRegistry meterRegistry,
                    MetricsCache metricsCache,
                    ServiceBrokerMetricsConfig serviceBrokerMetricsConfig) {
        return new BindingMetricsService(
                        meterRegistry,
                        metricsCache,
                        serviceBrokerMetricsConfig)
    }

    @Bean
    @ConditionalOnProperty(name = "management.metrics.export.influx.enabled", havingValue = "true")
    LastOperationMetricService getLastOperationMetricsService(
            MeterRegistry meterRegistry,
            MetricsCache metricsCache,
            ServiceBrokerMetricsConfig serviceBrokerMetricsConfig) {
        return new LastOperationMetricsService(
                meterRegistry,
                metricsCache,
                serviceBrokerMetricsConfig)
    }
}
