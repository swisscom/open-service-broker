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

package com.swisscom.cloud.sb.broker.services.mongodb.enterprise

import com.swisscom.cloud.sb.broker.BaseTransactionalSpecification
import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceDetailRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.util.DBTestUtil
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailKey
import org.springframework.beans.factory.annotation.Autowired

class MongoDbEnterpriseFreePortFinderSpec extends BaseTransactionalSpecification {
    private MongoDbEnterpriseFreePortFinder mongoDbEnterpriseFreePortFinder
    @Autowired
    private DBTestUtil dbTestUtil
    @Autowired
    private CFServiceRepository cfServiceRepository
    @Autowired
    private ServiceDetailRepository serviceDetailRepository
    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository

    def "happy path:port is selected from range correctly"() {
        given:
        mongoDbEnterpriseFreePortFinder = new MongoDbEnterpriseFreePortFinder(new MongoDbEnterpriseConfig(portRange: '27000-65000'), serviceInstanceRepository)
        expect:
        mongoDbEnterpriseFreePortFinder.findFreePorts(1)
    }

    def "when no free port is left an exception should be thrown"() {
        given:
        mongoDbEnterpriseFreePortFinder = new MongoDbEnterpriseFreePortFinder(new MongoDbEnterpriseConfig(portRange: '1000-1001'), serviceInstanceRepository)
        and:
        def plan = dbTestUtil.createPlan('plan1', UUID.randomUUID().toString(), true, 'test', 'id')
        and:
        CFService cfService = cfServiceRepository.save(new CFService(guid: UUID.randomUUID().toString(), name: 'MongoDbEnterpriseFreePortFinderSpec', internalName: ServiceProviderLookup.findInternalName(MongoDbEnterpriseServiceProvider)))
        cfService.plans.add(plan)
        cfServiceRepository.save(cfService)
        and:
        dbTestUtil.createServiceInstace(cfService, UUID.randomUUID().toString(), [ServiceDetail.from(ServiceDetailKey.PORT, '1000')])

        when:
        mongoDbEnterpriseFreePortFinder.findFreePorts(1)

        then:
        thrown(RuntimeException)
    }

    def "when an existing service instance contain no port information there should be no exception thrown"() {
        given:
        mongoDbEnterpriseFreePortFinder = new MongoDbEnterpriseFreePortFinder(new MongoDbEnterpriseConfig(portRange: '1000-1002'), serviceInstanceRepository)
        and:
        def plan = dbTestUtil.createPlan('plan1', UUID.randomUUID().toString(), true, 'test', 'id')
        and:
        CFService cfService = cfServiceRepository.save(new CFService(guid: UUID.randomUUID().toString(), name: 'MongoDbEnterpriseFreePortFinderSpec', internalName: ServiceProviderLookup.findInternalName(MongoDbEnterpriseServiceProvider)))
        cfService.plans.add(plan)
        cfServiceRepository.save(cfService)
        and:
        def serviceInstance = dbTestUtil.createServiceInstace(cfService, UUID.randomUUID().toString())
        def detail = serviceDetailRepository.save(ServiceDetail.from(ServiceDetailKey.PORT, '1000'))
        serviceInstance.details.add(detail)
        serviceInstanceRepository.save(serviceInstance)

        and:
        def serviceInstance2 = dbTestUtil.createServiceInstace(cfService, UUID.randomUUID().toString())
        expect:
        mongoDbEnterpriseFreePortFinder.findFreePorts(1).first() == 1001
    }

    def "when portRange is in wrong format, an exception should be thrown"() {
        given:
        mongoDbEnterpriseFreePortFinder = new MongoDbEnterpriseFreePortFinder(new MongoDbEnterpriseConfig(portRange: '27000'), serviceInstanceRepository)
        when:
        mongoDbEnterpriseFreePortFinder.findFreePorts(1)
        then:
        thrown(RuntimeException)
    }
}