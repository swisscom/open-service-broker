package com.swisscom.cloud.sb.broker.services.openwhisk

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.model.*
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import com.swisscom.cloud.sb.model.usage.ServiceUsage
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import static OpenWhiskServiceDetailKey.OPENWHISK_NAMESPACE
import static OpenWhiskServiceDetailKey.OPENWHISK_SUBJECT
import static com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper.from

class OpenWhiskServiceProviderSpec extends Specification{
    private final String NAMESPACE = "TEST_NAMESPACE"
    private final String SUBJECT = "TEST_SUBJECT"
    private final String UUID = "TEST_UUID"
    private final String KEY = "TEST_KEY"

    private OpenWhiskServiceProvider openWhiskServiceProvider
    private OpenWhiskConfig openWhiskConfig
    private MockRestServiceServer mockServer
    private ServiceInstanceRepository serviceInstanceRepository
    private ServiceBindingRepository serviceBindingRepository
    private RestTemplateBuilder restTemplateBuilder


    def setup() {
        serviceInstanceRepository = Mock(ServiceInstanceRepository)
        serviceBindingRepository = Mock(ServiceBindingRepository)

        and:
        RestTemplate restTemplate = new RestTemplate()
        mockServer = MockRestServiceServer.createServer(restTemplate)
        restTemplateBuilder = Mock(RestTemplateBuilder)
        restTemplateBuilder.build() >> restTemplate

        and:
        restTemplateBuilder.withBasicAuthentication(_, _) >> restTemplateBuilder

        and:
        openWhiskConfig = new OpenWhiskConfig(openWhiskDbProtocol: "http",
                openWhiskDbHost: "openwhiskHost", openWhiskDbPort: "1234", openWhiskDbLocalUser: "ubuntu",
                openWhiskDbHostname: "localhost", openWhiskPath: "/api/v1/")
        and:
        openWhiskServiceProvider = new OpenWhiskServiceProvider(openWhiskConfig,
                new OpenWhiskDbClient(openWhiskConfig, restTemplateBuilder), serviceInstanceRepository, serviceBindingRepository)
    }

    def "creating a new subject"() {
        given:
        mockServer.expect(MockRestRequestMatchers.requestTo("http://openwhiskHost:1234/ubuntu_localhost_subjects/${SUBJECT}"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.BAD_REQUEST))

        when:
        JsonNode res = openWhiskServiceProvider.createSubject(NAMESPACE, SUBJECT, UUID, KEY)

        then:
        res.path("subject").asText().trim() == SUBJECT
        res.path("namespaces").path(0).path("name").asText().trim() == NAMESPACE
        res.path("namespaces").path(0).path("uuid").asText().trim() == UUID
        res.path("namespaces").path(0).path("key").asText().trim() == KEY

        and:
        mockServer.verify()
    }

    def "adding a namespace to subject"() {
        given:
        mockServer.expect(MockRestRequestMatchers.requestTo("http://openwhiskHost:1234/ubuntu_localhost_subjects/${SUBJECT}"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess("""{"_id":"TEST_SUBJECT","_rev":"rev","subject":"TEST_SUBJECT","namespaces":[]}""", MediaType.TEXT_PLAIN))

        when:
        JsonNode res = openWhiskServiceProvider.createSubject(NAMESPACE, SUBJECT, UUID, KEY)

        then:
        res.path("subject").asText().trim() == SUBJECT
        res.path("namespaces").path(0).path("name").asText().trim() == NAMESPACE
        res.path("namespaces").path(0).path("uuid").asText().trim() == UUID
        res.path("namespaces").path(0).path("key").asText().trim() == KEY

        and:
        mockServer.verify()
    }

    def "namespace conflict within subject"() {
        given:
        mockServer.expect(MockRestRequestMatchers.requestTo("http://openwhiskHost:1234/ubuntu_localhost_subjects/${SUBJECT}"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess("""{"_id":"TEST_SUBJECT","_rev":"rev","subject":"TEST_SUBJECT","namespaces":[{"name":"TEST_NAMESPACE","uuid":"TEST_UUID","key":"TEST_KEY"}]}""", MediaType.TEXT_PLAIN))

        when:
        openWhiskServiceProvider.createSubject(NAMESPACE, SUBJECT, UUID, KEY)

        then:
        def exception = thrown(ServiceBrokerException)
        exception.httpStatus == HttpStatus.CONFLICT

        and:
        mockServer.verify()
    }

    def "delete entity with existing subject/namespace"() {
        given:
        def rev = "1-123456"
        mockServer.expect(MockRestRequestMatchers.requestTo("http://openwhiskHost:1234/ubuntu_localhost_subjects/${SUBJECT}"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess("""{"_id":"TEST_SUBJECT","_rev":"${rev}","subject":"TEST_SUBJECT","namespaces":[]}""", MediaType.TEXT_PLAIN))

        mockServer.expect(MockRestRequestMatchers.requestTo("http://openwhiskHost:1234/ubuntu_localhost_subjects/${SUBJECT}?rev=${rev}"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
                .andRespond(MockRestResponseCreators.withSuccess("", MediaType.TEXT_PLAIN))

        when:
        openWhiskServiceProvider.deleteEntity(SUBJECT)

        then:
        noExceptionThrown()

        and:
        mockServer.verify()
    }

    def "delete entity without existing subject/namespace"() {
        given:
        mockServer.expect(MockRestRequestMatchers.requestTo("http://openwhiskHost:1234/ubuntu_localhost_subjects/" + SUBJECT))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.BAD_REQUEST))

        when:
        openWhiskServiceProvider.deleteEntity(SUBJECT)

        then:
        def exception = thrown(ServiceBrokerException)
        exception.httpStatus == HttpStatus.BAD_REQUEST

        and:
        mockServer.verify()
    }

    def "get subject"() {
        given:
        def bindId = "bindId"
        ServiceBinding serviceBinding = new ServiceBinding(details: [ServiceDetail.from(OPENWHISK_SUBJECT, SUBJECT)])

        and:
        serviceBindingRepository.findByGuid(bindId) >> serviceBinding

        expect:
        openWhiskServiceProvider.getSubject(bindId) == SUBJECT
    }

    def "get namespace"() {
        given:
        def serviceId = "serviceId"
        ServiceInstance serviceInstance = new ServiceInstance(details: [ServiceDetail.from(OPENWHISK_NAMESPACE, NAMESPACE)])

        and:
        serviceInstanceRepository.findByGuid(serviceId) >> serviceInstance

        expect:
        openWhiskServiceProvider.getNamespace(serviceId) == NAMESPACE
    }

    def "provision service instance"() {
        given:
        ProvisionRequest provisionRequest = new ProvisionRequest(serviceInstanceGuid: "serviceId", plan: new Plan(), parameters: "{\"namespace\": \"${NAMESPACE}\"}")

        mockServer.expect(MockRestRequestMatchers.requestTo("http://openwhiskHost:1234/ubuntu_localhost_subjects/${NAMESPACE}"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess("""{"_id":"${SUBJECT}","_rev":"rev","subject":"${SUBJECT}","namespaces":[]}""", MediaType.TEXT_PLAIN))

        mockServer.expect(MockRestRequestMatchers.requestTo("http://openwhiskHost:1234/ubuntu_localhost_subjects"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withSuccess("", MediaType.TEXT_PLAIN))

        when:
        def provisionResponse = openWhiskServiceProvider.provision(provisionRequest)

        then:
        from(provisionResponse.details).getValue(OPENWHISK_NAMESPACE) == NAMESPACE
    }
        //TODO: Figure out how to dynamically change URI for mockServer
//    def "provision with missing namespace"() {
//        given:
//        ProvisionRequest provisionRequest = new ProvisionRequest(serviceInstanceGuid: "serviceId", plan: new Plan(), parameters: "{}")
//
//        mockServer.expect(MockRestRequestMatchers.requestTo("http://openwhiskHost:1234/ubuntu_localhost_subjects/${NAMESPACE}"))
//                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
//                .andRespond(MockRestResponseCreators.withSuccess("""{"_id":"${SUBJECT}","_rev":"rev","subject":"${SUBJECT}","namespaces":[]}""", MediaType.TEXT_PLAIN))
//
//        mockServer.expect(MockRestRequestMatchers.requestTo("http://openwhiskHost:1234/ubuntu_localhost_subjects"))
//                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
//                .andRespond(MockRestResponseCreators.withSuccess("", MediaType.TEXT_PLAIN))
//
//        when:
//        def provisionResponse = openWhiskServiceProvider.provision(provisionRequest)
//
//        then:
//        println(from(provisionResponse.details).getValue(OPENWHISK_NAMESPACE))
//
//    }

    def "provision with namespace length < 5 characters"() {
        given:
        ProvisionRequest provisionRequest = new ProvisionRequest(serviceInstanceGuid: "serviceId", plan: new Plan(), parameters: "{\"namespace\": \"test\"}")

        when:
        openWhiskServiceProvider.provision(provisionRequest)

        then:
        def exception = thrown(ServiceBrokerException)
        exception.httpStatus == HttpStatus.BAD_REQUEST

    }

    def "deprovision service instance"() {
        given:
        def serviceId = "serviceId"
        DeprovisionRequest deprovisionRequest = new DeprovisionRequest(serviceInstanceGuid: serviceId)
        ServiceInstance serviceInstance = new ServiceInstance(details: [ServiceDetail.from(OPENWHISK_NAMESPACE, NAMESPACE)])
        serviceInstanceRepository.findByGuid(serviceId) >> serviceInstance

        def rev = "1-123456"
        mockServer.expect(MockRestRequestMatchers.requestTo("http://openwhiskHost:1234/ubuntu_localhost_subjects/${NAMESPACE}"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess("""{"_id":"TEST_SUBJECT","_rev":"${rev}","subject":"TEST_SUBJECT","namespaces":[]}""", MediaType.TEXT_PLAIN))

        mockServer.expect(MockRestRequestMatchers.requestTo("http://openwhiskHost:1234/ubuntu_localhost_subjects/${NAMESPACE}?rev=${rev}"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
                .andRespond(MockRestResponseCreators.withSuccess("", MediaType.TEXT_PLAIN))

        when:
        def deprovisionResponse = openWhiskServiceProvider.deprovision(deprovisionRequest)

        then:
        deprovisionResponse.isAsync == false
        noExceptionThrown()

        and:
        mockServer.verify()
    }

    def "bind to service instance"() {
        given:
        def serviceId = "serviceId"
        ServiceInstance serviceInstance = new ServiceInstance(guid: serviceId, details: [ServiceDetail.from(OPENWHISK_NAMESPACE, NAMESPACE)])
        BindRequest bindRequest = new BindRequest(serviceInstance: serviceInstance, parameters: [subject: SUBJECT])
        serviceInstanceRepository.findByGuid(serviceId) >> serviceInstance

        def rev = "1-123456"
        mockServer.expect(MockRestRequestMatchers.requestTo("http://openwhiskHost:1234/ubuntu_localhost_subjects/${SUBJECT}"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess("""{"_id":"TEST_SUBJECT","_rev":"${rev}","subject":"TEST_SUBJECT","namespaces":[]}""", MediaType.TEXT_PLAIN))

        mockServer.expect(MockRestRequestMatchers.requestTo("http://openwhiskHost:1234/ubuntu_localhost_subjects"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withSuccess("", MediaType.TEXT_PLAIN))

        when:
        def bindResponse = openWhiskServiceProvider.bind(bindRequest)

        then:
        from(bindResponse.details).getValue(OPENWHISK_NAMESPACE) == NAMESPACE
        from(bindResponse.details).getValue(OPENWHISK_SUBJECT) == SUBJECT

        and:
        mockServer.verify()
    }

    def "bind with subject length < 5 characters"() {
        given:
        def serviceId = "serviceId"
        ServiceInstance serviceInstance = new ServiceInstance(guid: serviceId, details: [ServiceDetail.from(OPENWHISK_NAMESPACE, NAMESPACE)])
        BindRequest bindRequest = new BindRequest(serviceInstance: serviceInstance, parameters: [subject: "test"])
        serviceInstanceRepository.findByGuid(serviceId) >> serviceInstance

        when:
        openWhiskServiceProvider.bind(bindRequest)

        then:
        def exception = thrown(ServiceBrokerException)
        exception.httpStatus == HttpStatus.BAD_REQUEST

    }

    def "unbind from service instance"() {
        given:
        String serviceBindingId = "serviceBindingId"
        ServiceBinding serviceBinding = new ServiceBinding(guid: serviceBindingId, details: [ServiceDetail.from(OPENWHISK_SUBJECT, SUBJECT)])
        UnbindRequest unbindRequest = new UnbindRequest(binding: serviceBinding)
        serviceBindingRepository.findByGuid(serviceBindingId) >> serviceBinding

        def rev = "1-123456"
        mockServer.expect(MockRestRequestMatchers.requestTo("http://openwhiskHost:1234/ubuntu_localhost_subjects/${SUBJECT}"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess("""{"_id":"${SUBJECT}","_rev":"${rev}","subject":"${SUBJECT}","namespaces":[]}""", MediaType.TEXT_PLAIN))

        mockServer.expect(MockRestRequestMatchers.requestTo("http://openwhiskHost:1234/ubuntu_localhost_subjects/${SUBJECT}?rev=${rev}"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
                .andRespond(MockRestResponseCreators.withSuccess("", MediaType.TEXT_PLAIN))

        when:
        openWhiskServiceProvider.unbind(unbindRequest)

        then:
        noExceptionThrown()

        and:
        mockServer.verify()
    }

    def "subject provided"() {
        expect:
        openWhiskServiceProvider.validateSubject([subject: SUBJECT], UUID) == SUBJECT
    }

    def "subject is null"() {
        when:
        openWhiskServiceProvider.validateSubject(null, UUID) == UUID

        then:
        noExceptionThrown()
    }

    def "parameters doesn't contain subject"() {
        when:
        openWhiskServiceProvider.validateSubject([:], UUID) == UUID

        then:
        noExceptionThrown()
    }

    def "namespace provided"() {
        given:
        ObjectMapper mapper = new ObjectMapper()
        expect:
        openWhiskServiceProvider.validateNamespace(mapper.readTree("""{"namespace": "${NAMESPACE}"}"""), UUID) == NAMESPACE
    }

    def "namespace is null"() {
        when:
        openWhiskServiceProvider.validateNamespace(null, UUID) == UUID

        then:
        noExceptionThrown()
    }

    def "parameters doesn't contain namespace"() {
        given:
        ObjectMapper mapper = new ObjectMapper()
        when:
        openWhiskServiceProvider.validateNamespace(mapper.readTree("{}"), UUID) == UUID

        then:
        noExceptionThrown()
    }

    def "find usage of service instance"() {
        given:
        mockServer.expect(MockRestRequestMatchers.requestTo("http://openwhiskHost:1234/ubuntu_localhost_whisks/_design/meter/_view/namespace?key=%22${NAMESPACE}%22"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess("""{"total_rows":31,"offset":0,"rows":[
                                    {"id":"${NAMESPACE}/16db9f7ed84d483db08e7bda2dea4565","key":"${NAMESPACE}","value":12288},
                                    {"id":"${NAMESPACE}/69c97109a7f64ffc9f46fd644d24d1e7","key":"${NAMESPACE}","value":11520},
                                    {"id":"${NAMESPACE}/d816a0e8883d46daad74344d3385bd55","key":"${NAMESPACE}","value":12032},
                                    {"id":"${NAMESPACE}/f59d41cf7f9e41a8bab69c333443222e","key":"${NAMESPACE}","value":12288}
                                    ]}""", MediaType.TEXT_PLAIN))
        when:
        ServiceUsage serviceUsage = openWhiskServiceProvider.findUsage(new ServiceInstance(details: [ServiceDetail.from(OPENWHISK_NAMESPACE, NAMESPACE)]), null)
        then:
        serviceUsage.value == "48.128"
        noExceptionThrown()
    }
}
