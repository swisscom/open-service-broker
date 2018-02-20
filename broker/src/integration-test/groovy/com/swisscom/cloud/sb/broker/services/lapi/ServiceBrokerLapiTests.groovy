package com.swisscom.cloud.sb.broker.services.lapi

import com.swisscom.cloud.sb.broker.BaseSpecification
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import spock.lang.Ignore

@Ignore
class ServiceBrokerLapiTests extends BaseSpecification {

    private LapiServiceProvider lapiServiceProvider
    private String SERVICE_INSTANCE_GUID = "65d546f1-2c74-4871-9d5f-b5b0df1a8912"
    private String SERVICE_BINDING_GUID = "65d546f1-2c74-4871-9d5f-b5b0df1a7082"

    @Autowired
    private LapiConfig lapiConfig

    def "setup"() {
        def restTemplateBuilder = new RestTemplateBuilder()
        lapiServiceProvider = new LapiServiceProvider(restTemplateBuilder, lapiConfig)
    }

    def "provision a lapi service instance"() {
        given:
        ProvisionRequest provisionRequest = new ProvisionRequest(serviceInstanceGuid: SERVICE_INSTANCE_GUID , plan: new Plan())

        when:
        lapiServiceProvider.provision(provisionRequest)

        then:
        noExceptionThrown()
    }

    def "bind provisioned instance"() {
        given:
        ServiceInstance serviceInstance = new ServiceInstance(guid: SERVICE_INSTANCE_GUID)
        BindRequest bindRequest = new BindRequest(binding_guid: SERVICE_BINDING_GUID, serviceInstance: serviceInstance)

        when:
        lapiServiceProvider.provision(provisionRequest)
        lapiServiceProvider.bind(bindRequest)

        then:
        noExceptionThrown()
    }

    def "bind provisioned instance"() {
        given:
        ServiceInstance serviceInstance = new ServiceInstance(guid: SERVICE_INSTANCE_GUID)
        BindRequest bindRequest = new BindRequest(binding_guid: SERVICE_BINDING_GUID, serviceInstance: serviceInstance)

        when:
        lapiServiceProvider.bind(bindRequest)

        then:
        noExceptionThrown()
    }

    def "unbind from provisioned instance"() {
        given:
        ServiceInstance serviceInstance= new ServiceInstance(guid: SERVICE_INSTANCE_GUID)
        ServiceBinding serviceBinding= new ServiceBinding(guid: SERVICE_BINDING_GUID)
        UnbindRequest unbindRequest = new UnbindRequest(binding: serviceBinding, serviceInstance: serviceInstance)

        when:
        lapiServiceProvider.unbind(unbindRequest)

        then:
        noExceptionThrown()

    }

    def "deprovision lapi service instance"() {
        given:
        DeprovisionRequest deprovisionRequest = new DeprovisionRequest(serviceInstanceGuid: SERVICE_INSTANCE_GUID)

        when:
        lapiServiceProvider.deprovision(deprovisionRequest)

        then:
        noExceptionThrown()

    }

    def "provision the same service instance twice"() {
        given:
        ProvisionRequest provisionRequest = new ProvisionRequest(serviceInstanceGuid: SERVICE_INSTANCE_GUID , plan: new Plan())

        when:
        lapiServiceProvider.provision(provisionRequest)
        lapiServiceProvider.provision(provisionRequest)

        then:
        HttpClientErrorException e = thrown()
        e.statusCode == HttpStatus.BAD_REQUEST
    }
}
