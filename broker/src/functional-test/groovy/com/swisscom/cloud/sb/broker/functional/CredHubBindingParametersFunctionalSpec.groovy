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

import com.swisscom.cloud.sb.broker.binding.CredentialService
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.util.JsonHelper
import com.swisscom.cloud.sb.broker.util.StringGenerator
import com.swisscom.cloud.sb.broker.util.test.DummySynchronousServiceProvider
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.core.io.ClassPathResource
import spock.lang.IgnoreIf

@IgnoreIf({ !CredHubBindingParametersFunctionalSpec.checkCredHubConfigSet() })
class CredHubBindingParametersFunctionalSpec extends BaseFunctionalSpec {

    @Autowired
    private ServiceBindingRepository serviceBindingRepository
    @Autowired
    private CredentialService credentialService

    def setupSpec() {
        System.setProperty('http.nonProxyHosts', 'localhost|127.0.0.1|uaa.service.cf.internal|credhub.service.consul')
        System.setProperty('javax.net.ssl.keyStore', FileUtils.getFile('src/functional-test/resources/credhub_client.jks').toURI().getPath())
        System.setProperty('javax.net.ssl.keyStorePassword', 'changeit')
        System.setProperty('javax.net.ssl.trustStore', FileUtils.getFile('src/functional-test/resources/credhub_client.jks').toURI().getPath())
        System.setProperty('javax.net.ssl.trustStorePassword', 'changeit')
    }

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('SyncDummyInstancesRetrievable', ServiceProviderLookup.findInternalName(DummySynchronousServiceProvider.class), null, null, null, 0, true, true)
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "provision async service instance and bind with parameters store credhub"() {
        given:
        def serviceBindingGuid = StringGenerator.randomUuid()
        serviceLifeCycler.setServiceBindingId(serviceBindingGuid)
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false)

        String username = StringGenerator.randomUuid()
        String password = StringGenerator.randomUuid()

        when:
        serviceLifeCycler.bindServiceInstanceAndAssert(null, [username: username, password: password])

        then:
        noExceptionThrown()

        def serviceBinding = serviceBindingRepository.findByGuid(serviceBindingGuid)
        serviceBinding != null
        serviceBinding.credhubCredentialId != null
        serviceBinding.credentials == null
    }

    def "get binding credentials from CredHub"() {
        given:
        when:
        def bindingResponse = serviceBrokerClient.getServiceInstanceBinding(serviceLifeCycler.serviceInstanceId, serviceLifeCycler.serviceBindingId)

        then:
        noExceptionThrown()
        bindingResponse != null
        bindingResponse.body.credentials != null

        def credentials = JsonHelper.parse(bindingResponse.body.credentials, Map) as Map
        credentials.username != null
        credentials.password != null

    }

    def "deprovision async service instance and delete credential from CredHub"() {
        when:
        serviceLifeCycler.deleteServiceBindingAndServiceInstanceAndAssert()

        then:
        noExceptionThrown()
    }

    /**
     * Checks and returns true if credhub configuration is enabled, otherwise false.
     */
    static boolean checkCredHubConfigSet() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean()
        yaml.setResources(new ClassPathResource("application-broker.yml"))
        yaml.afterPropertiesSet()
        return StringUtils.equals(yaml.object.getProperty("spring.credhub.enable"), "true")
    }

}