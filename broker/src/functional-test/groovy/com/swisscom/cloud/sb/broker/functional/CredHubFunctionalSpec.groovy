package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.services.credhub.CredHubService
import com.swisscom.cloud.sb.broker.util.JsonHelper
import com.swisscom.cloud.sb.broker.util.StringGenerator
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.StringUtils
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.context.ApplicationContext
import org.springframework.core.io.ClassPathResource
import org.springframework.credhub.support.CredentialDetails
import org.springframework.credhub.support.user.UserCredential
import spock.lang.IgnoreIf
import spock.lang.Shared

import java.security.Security

@IgnoreIf({ !CredHubFunctionalSpec.checkCredHubConfigSet() })
class CredHubFunctionalSpec extends BaseFunctionalSpec {

    @Autowired
    private ApplicationContext applicationContext

    @Shared
    private String credentialId
    @Shared
    private String credentialName

    CredHubService credHubService

    static {
        Security.addProvider(new BouncyCastleProvider())
    }

    def setupSpec() {
        System.setProperty('http.nonProxyHosts', 'localhost|127.0.0.1|uaa.service.cf.internal|credhub.service.consul')
        System.setProperty('javax.net.ssl.keyStore', FileUtils.getFile('src/functional-test/resources/credhub_client.jks').toURI().getPath())
        System.setProperty('javax.net.ssl.keyStorePassword', 'changeit')
        System.setProperty('javax.net.ssl.trustStore', FileUtils.getFile('src/functional-test/resources/credhub_client.jks').toURI().getPath())
        System.setProperty('javax.net.ssl.trustStorePassword', 'changeit')
    }

    def setup() {
        credHubService = getCredHubService()
    }

    def "Write CredHub credential"() {
        given:
        credentialName = StringGenerator.randomUuid()
        String username = StringGenerator.randomUuid()
        String password = StringGenerator.randomUuid()

        when:
        CredentialDetails<UserCredential> credential = credHubService.writeCredential(credentialName, username, password)
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
        CredentialDetails<UserCredential> details = credHubService.getCredential(credentialId)

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

    CredHubService getCredHubService() {
        try {
            return applicationContext.getBean(CredHubService)
        } catch (NoSuchBeanDefinitionException e) {
            return null
        }
    }

    static boolean checkCredHubConfigSet() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean()
        yaml.setResources(new ClassPathResource("application.yml"))
        yaml.afterPropertiesSet()
        return StringUtils.isNotBlank(yaml.object.getProperty("spring.credhub.url"))
    }

}
