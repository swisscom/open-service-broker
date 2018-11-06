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

package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.model.repository.PlanRepository
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
class TestHelperService {

    @Autowired
    CFServiceRepository cfServiceRepository

    @Autowired
    PlanRepository planRepository

    void setServicesAndPlansToActive(List<CFService> cfServices, List<Plan> plans) {
        cfServices.each { service ->
            service.active = true
            cfServiceRepository.saveAndFlush(service)
        }

        plans.each { plan ->
            plan.active = true
            planRepository.saveAndFlush(plan)
        }
    }

    void setAllServicesAndPlansToActive() {
        def cfServices = cfServiceRepository.findAll()
        def plans = planRepository.findAll()
        setServicesAndPlansToActive(cfServices, plans)
    }
}
