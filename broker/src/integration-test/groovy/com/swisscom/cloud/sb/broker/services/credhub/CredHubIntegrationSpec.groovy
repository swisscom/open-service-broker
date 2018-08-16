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

package com.swisscom.cloud.sb.broker.services.credhub

import com.swisscom.cloud.sb.broker.BaseSpecification
import com.swisscom.cloud.sb.broker.binding.CredHubCredentialStoreStrategy
import com.swisscom.cloud.sb.broker.binding.CredentialService
import com.swisscom.cloud.sb.broker.util.JsonHelper
import com.swisscom.cloud.sb.broker.util.StringGenerator
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.core.io.ClassPathResource
import org.springframework.credhub.support.CredentialDetails
import org.springframework.credhub.support.certificate.CertificateCredential
import org.springframework.credhub.support.json.JsonCredential
import org.springframework.credhub.support.rsa.RsaCredential
import spock.lang.IgnoreIf
import spock.lang.Shared

@IgnoreIf({ !CredHubIntegrationSpec.checkCredHubConfigSet() })
class CredHubIntegrationSpec extends BaseSpecification {

    @Autowired
    private CredentialService credentialService

    @Autowired
    private DefaultCredHubConfig defaultCredHubConfig

    @Shared
    private String credentialId
    @Shared
    private String credentialName

    private List<String> testCredentialNames

    @Shared
    CredHubService credHubService

    @Autowired
    CredHubCredentialStoreStrategy credHubCredentialStoreStrategy

    def setupSpec() {
        System.setProperty('http.nonProxyHosts', 'localhost|127.0.0.1|uaa.service.cf.internal|credhub.service.consul')
        System.setProperty('javax.net.ssl.keyStore', FileUtils.getFile('src/functional-test/resources/credhub_client.jks').toURI().getPath())
        System.setProperty('javax.net.ssl.keyStorePassword', 'changeit')
        System.setProperty('javax.net.ssl.trustStore', FileUtils.getFile('src/functional-test/resources/credhub_client.jks').toURI().getPath())
        System.setProperty('javax.net.ssl.trustStorePassword', 'changeit')
    }

    def setup() {
        credHubService = credHubCredentialStoreStrategy.getCredHubService()
    }

    def cleanup() {
        if (testCredentialNames != null) {
            for (testCredentialName in testCredentialNames) {
                credHubService.deleteCredential(testCredentialName)
            }
            testCredentialNames = null
        }
    }

    def "Write CredHub credential"() {
        given:
        credentialName = StringGenerator.randomUuid()

        when:
        CredentialDetails<JsonCredential> credential = credHubService.writeCredential(credentialName, [username: StringGenerator.randomUuid(), password: StringGenerator.randomUuid()])
        assert credential != null
        credentialId = credential.id

        then:
        println('UserCredential: ' + JsonHelper.toJsonString(credential))
        println('CredentialName: ' + JsonHelper.toJsonString(credential.name))
        println('UserCredentials value' + JsonHelper.toJsonString(credential.value))
    }

    def "Get Credential by id"() {
        given:
        assert credentialId != null

        when:
        CredentialDetails<JsonCredential> details = credHubService.getCredential(credentialId)

        then:
        details != null
        println('UserCredential: ' + JsonHelper.toJsonString(details))
        println('CredentialName: ' + JsonHelper.toJsonString(details.name))
        println('UserCredentials value' + JsonHelper.toJsonString(details.value))
    }

    def "Delete Credential by name"() {
        given:
        assert credentialId != null
        String credentialName = credentialName

        when:
        credHubService.deleteCredential(credentialName)

        then:
        noExceptionThrown()
    }

    def "Generate RSA with CredHub"() {
        given:
        testCredentialNames = [StringGenerator.randomUuid()]

        when:
        CredentialDetails<RsaCredential> credential = credHubService.generateRSA(testCredentialNames[0])
        assert credential != null
        credentialId = credential.id

        then:
        println('RsaCredential: ' + JsonHelper.toJsonString(credential))
    }

    def "Generate CA Certificate with CredHub"() {
        given:
        testCredentialNames = [StringGenerator.randomUuid()]
        defaultCredHubConfig.commonName = "testone.service.consul"
        defaultCredHubConfig.certificateAuthority = true

        when:
        CredentialDetails<CertificateCredential> credential = credHubService.generateCertificate(testCredentialNames[0], defaultCredHubConfig)
        assert credential != null
        credentialId = credential.id

        then:
        println('CertificateCredential: ' + JsonHelper.toJsonString(credential))
    }

    def "Generate Certificate based on generated CA Certificate on CredHub"() {
        given:
        testCredentialNames = [StringGenerator.randomUuid(), StringGenerator.randomUuid()]
        defaultCredHubConfig.commonName = "testone.service.consul"
        defaultCredHubConfig.certificateAuthority = true
        CredentialDetails<CertificateCredential> caCredential = credHubService.generateCertificate(testCredentialNames[0], defaultCredHubConfig)

        defaultCredHubConfig.commonName = "testone.service.consul"
        defaultCredHubConfig.certificateAuthority = false
        defaultCredHubConfig.certificateAuthorityCredential = '/' + testCredentialNames[0]

        when:
        CredentialDetails<CertificateCredential> credential = credHubService.generateCertificate(testCredentialNames[1], defaultCredHubConfig)

        assert caCredential != null
        assert credential != null
        credentialId = credential.id

        then:
        println('CertificateCredential: ' + JsonHelper.toJsonString(credential))
    }

    static boolean checkCredHubConfigSet() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean()
        yaml.setResources(new ClassPathResource("application.yml"))
        yaml.afterPropertiesSet()
        return StringUtils.equals(yaml.object.getProperty("spring.credhub.enable"), "true")
    }

}
