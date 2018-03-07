package com.swisscom.cloud.sb.broker.services.genericserviceprovider

import com.swisscom.cloud.sb.broker.BaseSpecification
import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.Parameter
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.services.lapi.config.LapiConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

class ServiceBrokerServiceProviderTest extends BaseSpecification {
    private ServiceBrokerServiceProvider serviceBrokerServiceProvider
    private String SERVICE_INSTANCE_GUID = "65d546f1-2c74-4871-9d5f-b5b0df1a8912"
    private String SERVICE_BINDING_GUID = "65d546f1-2c74-4871-9d5f-b5b0df1a7082"
    private String SERVICE_DEFINITION_GUID = "65d546f1-2c74-4871-9d5f-b5b0df1a8913"
    private String PLAN_GUID = "65d546f1-2c74-4871-9d5f-b5b0df1a7083"
    private String BASE_URL = "http://localhost:4567"

    private Plan plan
    private CFService service

    @Autowired
    private LapiConfig lapiConfig

    def setup() {
        serviceBrokerServiceProvider = new ServiceBrokerServiceProvider()
        plan = new Plan(parameters: [new Parameter(name: "baseUrl", value: BASE_URL), new Parameter(name: "username", value: lapiConfig.lapiUsername), new Parameter(name: "password", value: lapiConfig.lapiPassword), new Parameter(name: "service-guid", value: SERVICE_DEFINITION_GUID), new Parameter(name: "plan-guid", value: PLAN_GUID)])
        service = new CFService(guid: "65d546f1-2c74-4871-9d5f-b5b0df1a8914")
    }

    def "provision a service instance"() {
        given:
        ProvisionRequest provisionRequest = new ProvisionRequest(serviceInstanceGuid: SERVICE_INSTANCE_GUID , plan: plan)

        when:
        serviceBrokerServiceProvider.provision(provisionRequest)

        then:
        noExceptionThrown()
    }

    def "bind the provisioned service instance"() {
        given:
        ServiceInstance serviceInstance = new ServiceInstance(guid: SERVICE_INSTANCE_GUID, plan: plan)
        BindRequest bindRequest = new BindRequest(binding_guid: SERVICE_BINDING_GUID, serviceInstance: serviceInstance, service: service, plan: plan)

        when:
        serviceBrokerServiceProvider.bind(bindRequest)

        then:
        noExceptionThrown()
    }

    def "unbind the provisioned and bound service instance"() {
        given:
        ServiceInstance serviceInstance= new ServiceInstance(guid: SERVICE_INSTANCE_GUID, plan: plan)
        ServiceBinding serviceBinding= new ServiceBinding(guid: SERVICE_BINDING_GUID)
        UnbindRequest unbindRequest = new UnbindRequest(binding: serviceBinding, serviceInstance: serviceInstance, service:  service)

        when:
        serviceBrokerServiceProvider.unbind(unbindRequest)

        then:
        noExceptionThrown()
    }

    def "deprovision service instance"() {
        given:
        ServiceInstance serviceInstance= new ServiceInstance(guid: SERVICE_INSTANCE_GUID, plan: plan)
        DeprovisionRequest deprovisionRequest = new DeprovisionRequest(serviceInstanceGuid: SERVICE_INSTANCE_GUID, serviceInstance: serviceInstance)

        when:
        serviceBrokerServiceProvider.deprovision(deprovisionRequest)

        then:
        noExceptionThrown()
    }

    /*def "provision the same service instance twice"() {
        given:
        ProvisionRequest provisionRequest = new ProvisionRequest(serviceInstanceGuid: SERVICE_INSTANCE_GUID , plan: new Plan())

        when:
        serviceBrokerServiceProvider.provision(provisionRequest)
        serviceBrokerServiceProvider.provision(provisionRequest)

        then:
        // Resource Access Error
        HttpClientErrorException e = thrown()
        e.statusCode == HttpStatus.BAD_REQUEST
    }*/
}
