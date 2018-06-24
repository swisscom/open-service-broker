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

import com.swisscom.cloud.sb.broker.util.Resource

class ServiceDefinitionFunctionalSpec extends BaseFunctionalSpec {
    private String serviceName = 'functionalTestServiceforServiceDefiniton'

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist(serviceName, serviceName)
    }

    def "service definition gets created/updated"() {
        given:
        String serviceId = serviceLifeCycler.cfService.guid
        when:
        def response = serviceBrokerClient.createOrUpdateServiceDefinition(Resource.readTestFileContent("/service-data/service1.json"))
        then:
        response.statusCodeValue == 200
    }

    def "service definition delete"() {
        given:
        String serviceId = serviceLifeCycler.cfService.guid
        when:
        def response = serviceBrokerClient.deleteServiceDefinition(serviceId)
        then:
        response.statusCodeValue == 200
    }
}