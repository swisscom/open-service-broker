/*
 * Copyright (c) 2019 Swisscom (Switzerland) Ltd.
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
import com.swisscom.cloud.sb.broker.services.credential.BindRequest
import com.swisscom.cloud.sb.broker.services.credential.BindResponse
import com.swisscom.cloud.sb.broker.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailKey
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.retry.annotation.EnableRetry
import spock.lang.Ignore

import java.util.concurrent.ConcurrentLinkedQueue

@Ignore
@EnableRetry
@Slf4j
class MongoDbEnterpriseServiceProviderTest extends BaseTransactionalSpecification {


    @Autowired
    MongoDbEnterpriseServiceProvider mongoDbEnterpriseServiceProvider

    @Autowired
    ServiceInstanceRepository serviceInstanceRepository

    def setup() {
    }

    def "simple bind request"() {
        given:
        def serviceInstance = serviceInstanceRepository.findByGuid('da35a96d-d14f-4ba9-9d04-84d60eca507b')
        def bindRequest = new BindRequest(
                serviceInstance: serviceInstance,
                binding_guid: UUID.randomUUID().toString(),
                service: serviceInstance.plan.service,
                plan: serviceInstance.plan,
                )

        when:
        def bindResponse = mongoDbEnterpriseServiceProvider.bind(bindRequest)

        then:
        noExceptionThrown()
        bindResponse
    }

    def "parallel bind requests"() {
        given:
        def serviceInstance = serviceInstanceRepository.findByGuid('4fd6b7e0-98b5-42e7-8e72-740552a9b724')
        serviceInstance = serviceInstanceRepository.findByGuid('da35a96d-d14f-4ba9-9d04-84d60eca507b')

        when:
        int numberOfThreads = 3
        List threads = new ArrayList()
        def bindingQueue = new ConcurrentLinkedQueue<BindResponse>()
        for (int i = 0; i < numberOfThreads; i++) {
            def t = new Thread({
                def bindRequest = new BindRequest(
                        serviceInstance: serviceInstance,
                        binding_guid: UUID.randomUUID().toString(),
                        service: serviceInstance.plan.service,
                        plan: serviceInstance.plan,
                        )
                def bindResponse = mongoDbEnterpriseServiceProvider.bind(bindRequest)
                log.info("New binding created: ${ServiceDetailsHelper.from(bindResponse.details).getValue(ServiceDetailKey.USER)}")
                bindingQueue.add(bindResponse)
            })
            t.start()
            threads.add(t)
            Thread.sleep(10 * 1000)
        }
        then:
        for (int i = 0; i < threads.size(); i++) {
            ((Thread) threads.get(i)).join()
        }
        noExceptionThrown()
        bindingQueue.findAll {it}.size() == numberOfThreads
    }

}
