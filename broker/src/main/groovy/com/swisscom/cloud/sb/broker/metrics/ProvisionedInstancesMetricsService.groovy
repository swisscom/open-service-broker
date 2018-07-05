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

import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.model.repository.PlanRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import groovy.transform.CompileStatic
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
@CompileStatic
class ProvisionedInstancesMetricsService extends ServiceBrokerMetricsService {

    private final String PROVISIONED_INSTANCES = "provisionedInstances"

    @Autowired
    ProvisionedInstancesMetricsService(LastOperationRepository lastOperationRepository, MetricsCache metricsCache, MeterRegistry meterRegistry) {
        super(lastOperationRepository, metricsCache)
        addMetricsToMeterRegistry(meterRegistry)
    }

    void addMetricsToMeterRegistry(MeterRegistry meterRegistry){
        super.addMetricsToMeterRegistry(meterRegistry, PROVISIONED_INSTANCES)
    }


    @Override
    boolean considerServiceInstance(ServiceInstance serviceInstance) {
        // service instance should only be considered if it hasn't been deleted, yet
        return !serviceInstance.deleted
    }

    @Override
    String tag() {
        return ProvisionedInstancesMetricsService.class.getSimpleName()
    }
}