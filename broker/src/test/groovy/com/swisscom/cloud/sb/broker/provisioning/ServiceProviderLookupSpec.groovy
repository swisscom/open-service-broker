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

package com.swisscom.cloud.sb.broker.provisioning

import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.services.openwhisk.OpenWhiskConfig
import com.swisscom.cloud.sb.broker.services.openwhisk.OpenWhiskDbClient
import com.swisscom.cloud.sb.broker.services.openwhisk.OpenWhiskServiceProvider
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import org.springframework.context.ApplicationContext
import spock.lang.Specification

class ServiceProviderLookupSpec extends Specification {

    private ServiceProviderLookup serviceProviderLookup
    private OpenWhiskServiceProvider openWhiskServiceProvider
    private ServiceInstanceRepository serviceInstanceRepository
    private ServiceBindingRepository serviceBindingRepository
    private ApplicationContext applicationContext
    private Plan plan

    void setup() {
        plan = new Plan()
        applicationContext = Mock(ApplicationContext)
        serviceProviderLookup = new ServiceProviderLookup(appContext: applicationContext)
        openWhiskServiceProvider = new OpenWhiskServiceProvider(new OpenWhiskConfig(), new OpenWhiskDbClient(new OpenWhiskConfig(), new RestTemplateBuilder()), serviceInstanceRepository, serviceBindingRepository)
    }

    def "find service provider by plan.serviceProviderName"() {
        given:
        plan.serviceProviderClass = "openWhiskServiceProvider"
        plan.internalName = null
        plan.service = null
        applicationContext.getBean("openWhiskServiceProvider") >> openWhiskServiceProvider

        when:
        serviceProviderLookup.findServiceProvider(plan) == openWhiskServiceProvider

        then:
        noExceptionThrown()
    }

    def "find service provider by plan.internalName"() {
        given:
        plan.serviceProviderClass = null
        plan.internalName = "openWhisk"
        plan.service = null
        serviceProviderLookup.findServiceProvider(plan) >> openWhiskServiceProvider

        when:
        serviceProviderLookup.findServiceProvider(plan) == openWhiskServiceProvider

        then:
        noExceptionThrown()
    }

    def "find service provider by plan.service.serviceProviderName"() {
        given:
        plan.serviceProviderClass = null
        plan.internalName = null
        plan.service = new CFService(serviceProviderClass: "openWhiskServiceProvider")
        serviceProviderLookup.findServiceProvider(plan) >> openWhiskServiceProvider

        when:
        serviceProviderLookup.findServiceProvider(plan) == openWhiskServiceProvider

        then:
        noExceptionThrown()
    }

    def "find service provider by plan.service.internalName"() {
        given:
        plan.serviceProviderClass = null
        plan.internalName = null
        plan.service = new CFService(internalName: "openWhisk")
        serviceProviderLookup.findServiceProvider(plan) >> openWhiskServiceProvider

        when:
        serviceProviderLookup.findServiceProvider(plan) == openWhiskServiceProvider

        then:
        noExceptionThrown()
    }
}
