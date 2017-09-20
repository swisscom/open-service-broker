package com.swisscom.cloud.sb.broker.functional

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.swisscom.cloud.sb.broker.services.openwhisk.OpenWhiskServiceProvider
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.web.client.RestTemplate
import spock.lang.IgnoreIf

import static com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup.findInternalName

@IgnoreIf({ !Boolean.valueOf(System.properties['com.swisscom.cloud.sb.broker.run3rdPartyDependentTests']) })
class OpenwhiskFunctionalSpec extends BaseFunctionalSpec {

    private RestTemplate restTemplate

    @Autowired
    RestTemplateBuilder restTemplateBuilder

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
        serviceLifeCycler.createServiceInstanceAndServiceBindingAndAssert(1, false, false)
        def credentials = serviceLifeCycler.getCredentials()
        println("Credentials: ${credentials}")
        then:
        noExceptionThrown()
    }

    def "Create, execute, delete an action"() {
        when:
        def credentials = serviceLifeCycler.getCredentials()
        restTemplate = restTemplateBuilder.withSSLValidationDisabled().withBasicAuthentication(credentials.get("uuid"), credentials.get("key")).build()
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        createPackage(credentials, headers)
        createAction(credentials, headers)
        executeAction(credentials)
        serviceBrokerClient.getUsage(serviceLifeCycler.serviceInstanceId)
        deleteAction(credentials)
        deletePackage(credentials)

//        deleteActivation()

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

    void createPackage(Map credentials, HttpHeaders headers){
        String packageUrl = credentials.get("adminUrl") + "/" + credentials.get("namespace") + "/packages/OWTestPackage1"
        ResponseEntity<String> packageRes = restTemplate.exchange(packageUrl, HttpMethod.PUT, new HttpEntity<String>("""{"version": "0.0.1", "publish": true}""", headers), String.class)
        println("packageRes = ${packageRes}")
    }

    void createAction(Map credentials, HttpHeaders headers){
        String actionPayload = """{"version": "0.0.1",
                "publish": false,
                "exec": {
                    "kind": "nodejs:6",
                    "code": "function main() {return {payload: 'Hello world'};}"
                },
                "annotations": [
                    {
                        "key": "web-export",
                        "value": true
                    },
                    {
                        "key": "raw-http",
                        "value": false
                    },
                    {
                        "key": "final",
                        "value": true
                    }
                ]}"""
        HttpEntity<String> actionEntity = new HttpEntity<String>(actionPayload, headers)
        String actionUrl = credentials.get("adminUrl") + "/" + credentials.get("namespace") + "/actions/OWTestPackage1/OWTestAction"
        ResponseEntity<String> actionRes = restTemplate.exchange(actionUrl, HttpMethod.PUT, actionEntity, String.class)

        assert actionRes.statusCodeValue == 200 || actionRes.statusCodeValue == 202
    }

    void executeAction(Map credentials) {
        String executionURL = credentials.get("executionUrl") + "/OWTestPackage1/OWTestAction.json"
        ResponseEntity<String> executionRes = restTemplate.getForEntity(executionURL, String.class)

        ObjectMapper mapper = new ObjectMapper()
        JsonNode params = mapper.readTree(executionRes.getBody())
        String helloRes = params.path("payload").asText().trim()

        assert helloRes == "Hello world"
    }

    void deleteActivation(){
//        TODO: Delete activation from DB
//        String activationUrl = credentials.get("adminUrl") + "/" + credentials.get("namespace") + "/activations"
//        ResponseEntity<String> activationRes =  restTemplate.getForEntity(activationUrl, String.class)
//        println("activationRes = ${activationRes}")
    }

    void deleteAction(Map credentials){
        String actionUrl = credentials.get("adminUrl") + "/" + credentials.get("namespace") + "/actions/OWTestPackage1/OWTestAction"
        ResponseEntity<String> actionRes =  restTemplate.exchange(actionUrl, HttpMethod.DELETE,null, String.class)
        assert actionRes.statusCodeValue == 200
    }

    void deletePackage(Map credentials){
        String packageUrl = credentials.get("adminUrl") + "/" + credentials.get("namespace") + "/packages/OWTestPackage1"
        ResponseEntity<String> packageRes =  restTemplate.exchange(packageUrl, HttpMethod.DELETE,null, String.class)
        assert packageRes.statusCodeValue == 200
    }
}
