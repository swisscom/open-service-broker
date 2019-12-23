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
import com.swisscom.cloud.sb.broker.services.credential.CredHubCredentialStore
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
import org.springframework.test.context.ActiveProfiles
import spock.lang.IgnoreIf
import spock.lang.Shared

@IgnoreIf({ !BoshCredHubIntegrationSpec.checkCredHubConfigSet() })
@ActiveProfiles("info,default,extensions,secrets,test")
class BoshCredHubIntegrationSpec extends BaseSpecification {

    @Shared
    private String credentialId
    @Shared
    private String credentialName

    private List<String> testCredentialNames

    @Shared
    CredHubService boshCredHubService

    @Autowired
    CredHubCredentialStore credentialStore

    def setupSpec() {
        System.setProperty('http.nonProxyHosts', 'localhost|127.0.0.1|uaa.service.cf.internal|credhub.service.consul')
        System.setProperty('javax.net.ssl.keyStore', FileUtils.getFile('src/functional-test/resources/credhub_client.jks').toURI().getPath())
        System.setProperty('javax.net.ssl.keyStorePassword', 'changeit')
        System.setProperty('javax.net.ssl.trustStore', FileUtils.getFile('src/functional-test/resources/credhub_client.jks').toURI().getPath())
        System.setProperty('javax.net.ssl.trustStorePassword', 'changeit')
    }

    def setup() {
        boshCredHubService = boshCredHubTemplate.buildCredHubService()
    }

    def cleanup() {
        if (testCredentialNames != null) {
            for (testCredentialName in testCredentialNames) {
                boshCredHubService.deleteCredential(testCredentialName)
            }
            testCredentialNames = null
        }
    }

    def "Write CredHub credential"() {
        given:
        credentialName = StringGenerator.randomUuid()

        when:
        CredentialDetails<JsonCredential> credential = boshCredHubService.writeCredential(credentialName, [username: StringGenerator.randomUuid(), password: StringGenerator.randomUuid()])
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
        CredentialDetails<JsonCredential> details = boshCredHubService.getCredential(credentialId)

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
        boshCredHubService.deleteCredential(credentialName)

        then:
        noExceptionThrown()
    }

    def "Generate RSA with CredHub"() {
        given:
        testCredentialNames = [StringGenerator.randomUuid()]

        when:
        CredentialDetails<RsaCredential> credential = boshCredHubService.generateRSA(testCredentialNames[0])
        assert credential != null
        credentialId = credential.id

        then:
        println('RsaCredential: ' + JsonHelper.toJsonString(credential))
    }

    def "Generate CA Certificate with CredHub"() {
        given:
        testCredentialNames = [StringGenerator.randomUuid()]
        boshCredHubConfig.commonName = 'testone.service.consul'
        boshCredHubConfig.certificateAuthority = true

        when:
        CredentialDetails<CertificateCredential> credential = boshCredHubService.generateCertificate(testCredentialNames[0], boshCredHubConfig)
        assert credential != null
        credentialId = credential.id

        then:
        println('CertificateCredential: ' + JsonHelper.toJsonString(credential))
    }

    def "Generate Certificate based on generated CA Certificate on CredHub"() {
        given:
        testCredentialNames = [StringGenerator.randomUuid(), StringGenerator.randomUuid()]
        boshCredHubConfig.commonName = 'testone.service.consul'
        boshCredHubConfig.certificateAuthority = true
        CredentialDetails<CertificateCredential> caCredential = boshCredHubService.generateCertificate(testCredentialNames[0], boshCredHubConfig)

        boshCredHubConfig.certificateAuthority = false
        boshCredHubConfig.certificateAuthorityCredential = '/' + testCredentialNames[0]

        when:
        CredentialDetails<CertificateCredential> credential = boshCredHubService.generateCertificate(testCredentialNames[1], boshCredHubConfig)

        assert caCredential != null
        assert credential != null
        credentialId = credential.id

        then:
        println('CertificateCredential: ' + JsonHelper.toJsonString(credential))
    }

    static boolean checkCredHubConfigSet() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean()
        yaml.setResources(new ClassPathResource("application-test.yml"))
        yaml.afterPropertiesSet()
        return StringUtils.equals(yaml.object.getProperty("osb.bosh.credhub.enable"), "true")
    }

}
