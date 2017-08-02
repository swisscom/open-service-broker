package com.swisscom.cloud.sb.broker.services.openwhisk

import com.fasterxml.jackson.databind.JsonNode
import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.util.RestTemplateFactory
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import static com.swisscom.cloud.sb.broker.util.ServiceDetailKey.OPENWHISK_NAMESPACE
import static com.swisscom.cloud.sb.broker.util.ServiceDetailKey.OPENWHISK_SUBJECT
import static com.swisscom.cloud.sb.broker.util.ServiceDetailsHelper.from

class OpenWhiskServiceProviderSpec extends Specification{
    private final String NAMESPACE = "TEST_NAMESPACE"
    private final String SUBJECT = "TEST_SUBJECT"
    private final String UUID = "TEST_UUID"
    private final String KEY = "TEST_KEY"

    private OpenWhiskServiceProvider openWhiskServiceProvider
    private OpenWhiskConfig openWhiskConfig
    private RestTemplateFactory restTemplateFactory
    private MockRestServiceServer mockServer
    private ServiceInstanceRepository serviceInstanceRepository
    private ServiceBindingRepository serviceBindingRepository

    def setup() {
        setupMockInstances()
        setupMockServer()
        openWhiskConfig = new OpenWhiskConfig(openWhiskDbProtocol: "http",
                openWhiskDbHost: "openwhiskHost", openWhiskDbPort: "1234", openWhiskDbLocalUser: "ubuntu",
                openWhiskDbHostname: "localhost", openWhiskPath: "/api/v1/")
        and:
        openWhiskServiceProvider = new OpenWhiskServiceProvider(openWhiskConfig, restTemplateFactory,
                new OpenWhiskDbClient(openWhiskConfig, restTemplateFactory), serviceInstanceRepository, serviceBindingRepository)
    }

    def setupMockInstances() {
        restTemplateFactory = Mock(RestTemplateFactory)
        serviceInstanceRepository = Mock(ServiceInstanceRepository)
        serviceBindingRepository = Mock(ServiceBindingRepository)
    }

    def setupMockServer() {
        RestTemplate restTemplate = new RestTemplate()
        restTemplateFactory.buildWithBasicAuthentication(_,_) >> restTemplate
        mockServer = MockRestServiceServer.createServer(restTemplate)
    }

    def "creating a new subject"() {
        given:
        mockServer.expect(MockRestRequestMatchers.requestTo("http://openwhiskHost:1234/ubuntu_localhost_subjects/${SUBJECT}"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.BAD_REQUEST))

        when:
        JsonNode res = openWhiskServiceProvider.subjectHelper(NAMESPACE, SUBJECT, UUID, KEY)

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
        JsonNode res = openWhiskServiceProvider.subjectHelper(NAMESPACE, SUBJECT, UUID, KEY)

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
        openWhiskServiceProvider.subjectHelper(NAMESPACE, SUBJECT, UUID, KEY)

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

    def "provision with missing namespace"() {
        given:
        ProvisionRequest provisionRequest = new ProvisionRequest(serviceInstanceGuid: "serviceId", plan: new Plan(), parameters: "{}")

        when:
        openWhiskServiceProvider.provision(provisionRequest)

        then:
        def exception = thrown(ServiceBrokerException)
        exception.httpStatus == HttpStatus.BAD_REQUEST

    }

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
        Map<String, Object> bindingParams = new HashMap<String, Object>()
        bindingParams.put("subject", SUBJECT)
        BindRequest bindRequest = new BindRequest(serviceInstance: serviceInstance, parameters: bindingParams)
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
        Map<String, Object> bindingParams = new HashMap<String, Object>()
        bindingParams.put("subject", "test")
        BindRequest bindRequest = new BindRequest(serviceInstance: serviceInstance, parameters: bindingParams)
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
                .andRespond(MockRestResponseCreators.withSuccess("""{"_id":"TEST_SUBJECT","_rev":"${rev}","subject":"TEST_SUBJECT","namespaces":[]}""", MediaType.TEXT_PLAIN))

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
}
