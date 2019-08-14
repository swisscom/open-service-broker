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


import com.swisscom.cloud.sb.broker.util.test.DummyExtension.DummyExtensionsServiceProvider
import com.swisscom.cloud.sb.client.model.ProvisionResponseDto
import org.springframework.http.ResponseEntity
import org.yaml.snakeyaml.Yaml

import static com.swisscom.cloud.sb.broker.services.ServiceProviderLookup.findInternalName

class ExtensionProviderFunctionalSpec extends BaseFunctionalSpec {

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('extensionServiceProvider',
                                                      findInternalName(DummyExtensionsServiceProvider.class),
                                                      null,
                                                      null,
                                                      "dummyExtensions")
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "Create service and verify extension"() {
        when:
        ResponseEntity<ProvisionResponseDto> res = serviceLifeCycler.provision(false, null, [] as Map)
        then:
        "DummyExtensionURL" == res.body.extension_apis[0].discovery_url
    }

    def "Get api docs"() {
        when:
        String res = serviceBrokerClient.getApi(serviceLifeCycler.serviceInstanceId).body
        Yaml parser = new Yaml()
        parser.load(res)
        then:
        noExceptionThrown()
    }
}
