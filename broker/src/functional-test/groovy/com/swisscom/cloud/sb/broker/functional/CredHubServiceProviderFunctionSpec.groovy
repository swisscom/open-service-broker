package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.services.credhub.CredHubServiceProvider
import groovy.json.JsonSlurper
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.core.io.ClassPathResource
import org.springframework.credhub.core.CredHubOperations
import org.springframework.credhub.core.interpolation.CredHubInterpolationOperations
import org.springframework.credhub.core.interpolation.CredHubInterpolationTemplate
import org.springframework.credhub.support.ServicesData
import org.springframework.test.context.ActiveProfiles
import spock.lang.IgnoreIf
import spock.lang.Shared

@IgnoreIf({ !CredHubServiceProviderFunctionSpec.checkCredHubConfigSet() })
@ActiveProfiles("info,default,extensions,secrets,test")
class CredHubServiceProviderFunctionSpec extends BaseFunctionalSpec {

    @Autowired
    private CredHubOperations credHubOperations

    @Shared
    Map<String, Object> creds

    def setupSpec() {
        System.setProperty('http.nonProxyHosts', 'localhost|127.0.0.1|uaa.service.cf.internal|credhub.service.consul')
        System.setProperty('javax.net.ssl.keyStore', FileUtils.getFile('src/functional-test/resources/credhub_client.jks').toURI().getPath())
        System.setProperty('javax.net.ssl.keyStorePassword', 'changeit')
        System.setProperty('javax.net.ssl.trustStore', FileUtils.getFile('src/functional-test/resources/credhub_client.jks').toURI().getPath())
        System.setProperty('javax.net.ssl.trustStorePassword', 'changeit')
    }

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('credHubTest', ServiceProviderLookup.findInternalName(CredHubServiceProvider), null, null, null, 0, true, true)
    }

    def "provision chaas service instance"() {
        when:
        serviceLifeCycler.createServiceInstanceAndServiceBindingAndAssert(0, false,false, null, ["password":"pass"])
        creds = serviceLifeCycler.getCredentials()
        println("Credentials: ${creds.get("credhub-ref")}")

        then:
        noExceptionThrown()

    }

    def "interpolate credhub ref"() {
        when:
        def jsonSlurper = new JsonSlurper()
        def object = jsonSlurper.parseText("""{"chaas": [{ "credentials": { "credhub-ref": "${creds.get("credhub-ref")}" }, "instance_name": "creds", "label": "chaas", "name": "creds", "plan": "basic", "tags": [] }]}""")
        ServicesData data = new ServicesData(object as HashMap)
        CredHubInterpolationOperations credHubInterpolationOperations = new CredHubInterpolationTemplate(credHubOperations)

        then:
        ServicesData newData = credHubInterpolationOperations.interpolateServiceData(data)
        assert newData.get("chaas").get(0).get("credentials") == ["password":"pass"]
    }

    def "deprovision openwhisk service instance"() {
        when:
        serviceLifeCycler.deleteServiceBindingAndAssert()
        serviceLifeCycler.deleteServiceInstanceAndAssert()

        then:
        noExceptionThrown()
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    static boolean checkCredHubConfigSet() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean()
        yaml.setResources(new ClassPathResource("application-test.yml"))
        yaml.afterPropertiesSet()
        return StringUtils.equals(yaml.object.getProperty("spring.credhub.enable"), "true")
    }
}
