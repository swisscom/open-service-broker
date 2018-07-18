/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.broker.metrics

import com.swisscom.cloud.sb.broker.model.Plan
import groovy.transform.CompileStatic
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry

import java.util.function.ToDoubleFunction

import java.time.Duration

@CompileStatic
abstract class ServiceBrokerMetricsService {

    protected ServiceBrokerMetricsConfig serviceBrokerMetricsConfig
    protected MetricsCache metricsCache
    protected MeterRegistry meterRegistry

    protected ServiceBrokerMetricsService(MeterRegistry meterRegistry, MetricsCache metricsCache, ServiceBrokerMetricsConfig serviceBrokerMetricsConfig) {
        this.meterRegistry = meterRegistry
        this.metricsCache = metricsCache
        this.serviceBrokerMetricsConfig = serviceBrokerMetricsConfig

        bindAll()
    }

    void addMetricsGauge(String name, Closure<Double> function, Map<String, String> tags = new HashMap<String, String>()) {
        ToDoubleFunction<ServiceBrokerMetricsService> doubleFunction = new ToDoubleFunction<ServiceBrokerMetricsService>() {
            @Override
            double applyAsDouble(ServiceBrokerMetricsService serviceBrokerMetrics) {
                function()
            }
        }

        def gaugeBuilder = Gauge.builder(name, this, doubleFunction)
        tags.put( "env", serviceBrokerMetricsConfig.env)
        tags.each { tagName, tagValue -> gaugeBuilder = gaugeBuilder.tag(tagName, tagValue) }
        gaugeBuilder.register(meterRegistry)
    }

    Counter createMetricsCounter(String name, Map<String, String> tags = new HashMap<String, String>()) {
        def counterBuilder = Counter.builder(name)
        tags.put( "env", serviceBrokerMetricsConfig.env)
        tags.each { t -> counterBuilder.tag(t.key, t.value) }
        counterBuilder.register(meterRegistry)
    }

    abstract void bindAll()
}