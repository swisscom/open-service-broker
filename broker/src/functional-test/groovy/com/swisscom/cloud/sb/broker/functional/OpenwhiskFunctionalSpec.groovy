package com.swisscom.cloud.sb.broker.functional

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.swisscom.cloud.sb.broker.services.openwhisk.OpenWhiskServiceProvider
import com.swisscom.cloud.sb.broker.util.RestTemplateFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

import static com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup.findInternalName
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.IgnoreIf


@IgnoreIf({ !Boolean.valueOf(System.properties['com.swisscom.cloud.sb.broker.run3rdPartyDependentTests']) })
class OpenwhiskFunctionalSpec extends BaseFunctionalSpec {

    private RestTemplate restTemplate

    @Autowired
    private RestTemplateFactory restTemplatefactory

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('openwhiskTest', findInternalName(OpenWhiskServiceProvider))
        def plan = serviceLifeCycler.plan
        serviceLifeCycler.createParameter("openwhiskName", "openwhiskValue", plan)
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "provision openwhisk service instance"() {
        when:
            Map<String, Object> serviceParams = new HashMap<String, Object>()
            serviceParams.put("namespace", "OWTestNamespace")
            serviceLifeCycler.createServiceInstanceAndAssert(1, false, false, serviceParams)
            Map<String, Object> bindingParams = new HashMap<String, Object>()
            bindingParams.put("subject", "OWTestSubject")
            serviceLifeCycler.bindServiceInstanceAndAssert(null, bindingParams)
            println("Created serviceInstanceId:${serviceLifeCycler.serviceInstanceId} , serviceBindingId ${serviceLifeCycler.serviceBindingId}")
            def credentials = serviceLifeCycler.getCredentials()
            println("Credentials: ${credentials}")
        then:
            noExceptionThrown()
    }

    def "create and execute action"() {
        when:
            def credentials = serviceLifeCycler.getCredentials()
            restTemplate = restTemplatefactory.buildWithSSLValidationDisabledAndBasicAuthentication(credentials.get("uuid"), credentials.get("key"))
            String packagePayload = "{\"version\": \"0.0.1\", \"publish\": true}"
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> packageEntity = new HttpEntity<String>(packagePayload, headers)
            String packageUrl = credentials.get("adminUrl") + "/" + credentials.get("namespace") + "/packages/OWTestPackage1"
            ResponseEntity<String> packageRes = restTemplate.exchange(packageUrl, HttpMethod.PUT, packageEntity, String.class)
            println("packageRes = ${packageRes}")
            String actionPayload = "{" +
                    "\"version\": \"0.0.1\", " +
                    "\"publish\": false, " +
                    "\"exec\": { " +
                        "\"kind\": \"nodejs:6\", " +
                        "\"code\": \"function main() {return {payload: 'Hello world'};}\" " +
                    "}, " +
                    "\"annotations\": [ " +
                        "{ " +
                            "\"key\": \"web-export\"," +
                            "\"value\": true " +
                        "}, " +
                        "{ " +
                            "\"key\": \"raw-http\", " +
                            "\"value\": false " +
                        "}, " +
                        "{ " +
                            "\"key\": \"final\", " +
                            "\"value\": true " +
                        "}" +
                    "]}"
            HttpEntity<String> actionEntity = new HttpEntity<String>(actionPayload, headers)
            String actionUrl = credentials.get("adminUrl") + "/" + credentials.get("namespace") + "/actions/OWTestPackage1/OWTestAction"
            ResponseEntity<String> actionRes = restTemplate.exchange(actionUrl, HttpMethod.PUT, actionEntity, String.class)

            assert actionRes.statusCodeValue == 200 || actionRes.statusCodeValue == 202

            String executionURL = credentials.get("executionUrl") + "/OWTestPackage1/OWTestAction.json"
            ResponseEntity<String> executionRes = restTemplate.getForEntity(executionURL, String.class)

            ObjectMapper mapper = new ObjectMapper()
            JsonNode params = mapper.readTree(executionRes.getBody())
            String helloRes = params.path("payload").asText().trim()

            assert helloRes == "Hello world"
        then:
            noExceptionThrown()

    }

    def "cleanup action and package"() {
        when:
            def credentials = serviceLifeCycler.getCredentials()
            restTemplate = restTemplatefactory.buildWithSSLValidationDisabledAndBasicAuthentication(credentials.get("uuid"), credentials.get("key"))

            String actionUrl = credentials.get("adminUrl") + "/" + credentials.get("namespace") + "/actions/OWTestPackage1/OWTestAction"
            ResponseEntity<String> actionRes =  restTemplate.exchange(actionUrl, HttpMethod.DELETE,null, String.class)
            assert actionRes.statusCodeValue == 200

            String packageUrl = credentials.get("adminUrl") + "/" + credentials.get("namespace") + "/packages/OWTestPackage1"
            ResponseEntity<String> packageRes =  restTemplate.exchange(packageUrl, HttpMethod.DELETE,null, String.class)
            assert packageRes.statusCodeValue == 200

//            TODO: Delete activation from DB
//            String activationUrl = credentials.get("adminUrl") + "/" + credentials.get("namespace") + "/activations"
//            ResponseEntity<String> activationRes =  restTemplate.getForEntity(activationUrl, String.class)
//            println("activationRes = ${activationRes}")

        then:
            noExceptionThrown()
    }

    def "deprovision openwhisk service instance"() {
        when:
            serviceLifeCycler.deleteServiceBindingAndAssert()
            serviceLifeCycler.deleteServiceInstanceAndAssert()
            serviceLifeCycler.pauseExecution(1)

        then:
            noExceptionThrown()
    }
}
