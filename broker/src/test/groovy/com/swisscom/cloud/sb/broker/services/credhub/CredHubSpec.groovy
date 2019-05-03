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

import com.swisscom.cloud.sb.broker.util.JsonHelper
import com.swisscom.cloud.sb.broker.util.StringGenerator
import com.swisscom.cloud.sb.broker.util.test.category.DockerTest
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.StringUtils
import org.junit.experimental.categories.Category
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.core.io.ClassPathResource
import org.springframework.credhub.support.CredentialDetails
import org.springframework.credhub.support.certificate.CertificateCredential
import org.springframework.credhub.support.json.JsonCredential
import org.springframework.credhub.support.permissions.Permission
import org.springframework.credhub.support.rsa.RsaCredential
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared

@Category([DockerTest.class])
@ActiveProfiles("credhub")
@Testcontainers
@ContextConfiguration
@SpringBootTest(properties = "spring.autoconfigure.exclude=com.swisscom.cloud.sb.broker.util.httpserver.WebSecurityConfig")
@ComponentScan(
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASPECTJ,
                pattern = "com.swisscom.cloud.sb.broker.util.httpserver.*"))
class CredHubSpec extends AbstractCredHubSpec {

    private static final Logger LOG = LoggerFactory.getLogger(CredHubSpec.class)

    @Autowired
    private DefaultCredHubConfig defaultCredHubConfig

    @Shared
    private String credentialId
    @Shared
    private String credentialName
    @Shared
    private String appGuid

    private List<String> testCredentialNames

    @Autowired
    CredHubService credHubService

    def setupSpec() {
        System.setProperty('http.nonProxyHosts', 'localhost|127.0.0.1|uaa.service.cf.internal|credhub.service.consul')
        System.setProperty('javax.net.ssl.keyStore',
                           FileUtils.getFile('src/functional-test/resources/credhub_client.jks').toURI().getPath())
        System.setProperty('javax.net.ssl.keyStorePassword', 'changeit')
        System.setProperty('javax.net.ssl.trustStore',
                           FileUtils.getFile('src/functional-test/resources/credhub_client.jks').toURI().getPath())
        System.setProperty('javax.net.ssl.trustStorePassword', 'changeit')
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
        CredentialDetails<JsonCredential> credential = credHubService.writeCredential(credentialName,
                                                                                      [username                     : StringGenerator.
                                                                                              randomUuid(), password: StringGenerator.
                                                                                              randomUuid()])
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

    def "Add Permission"() {
        given:
        assert credentialName != null
        appGuid = "appGUID"

        when:
        credHubService.addReadPermission(credentialName, appGuid)

        then:
        noExceptionThrown()
    }

    def "Get Permissions"() {
        given:
        assert credentialName != null

        when:
        List<Permission> permissions = credHubService.getPermissions(credentialName)
        Boolean flag = false
        for (item in permissions) {
            if (item.actor.primaryIdentifier == appGuid) {
                flag = true
                break
            }
        }

        then:
        assert flag
    }

    def "Delete Permission"() {
        given:
        assert credentialName != null

        when:
        credHubService.deletePermission(credentialName, appGuid)

        then:
        noExceptionThrown()
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
        CredentialDetails<CertificateCredential> credential = credHubService.generateCertificate(testCredentialNames[0],
                                                                                                 defaultCredHubConfig)
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
        CredentialDetails<CertificateCredential> caCredential = credHubService.generateCertificate(testCredentialNames[0],
                                                                                                   defaultCredHubConfig)

        defaultCredHubConfig.commonName = "testone.service.consul"
        defaultCredHubConfig.certificateAuthority = false
        defaultCredHubConfig.certificateAuthorityCredential = '/' + testCredentialNames[0]

        when:
        CredentialDetails<CertificateCredential> credential = credHubService.generateCertificate(testCredentialNames[1],
                                                                                                 defaultCredHubConfig)

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
        return StringUtils.equals(yaml.object.getProperty("osb.credhub.enable"), "true")
    }

}
