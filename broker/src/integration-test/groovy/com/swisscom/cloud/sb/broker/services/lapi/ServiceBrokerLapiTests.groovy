package com.swisscom.cloud.sb.broker.services.lapi

import com.swisscom.cloud.sb.broker.BaseSpecification
import com.swisscom.cloud.sb.broker.BaseTransactionalSpecification
import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceInstance

class ServiceBrokerLapiTests extends BaseTransactionalSpecification {

    private LapiServiceProvider lapiServiceProvider

    def "setup"() {
        lapiServiceProvider = new LapiServiceProvider()
    }

    def "provision & bind a lapi service instance"() {
        given:
        def serviceInstanceGuid = "serviceInstanceGuid"
        def provisionRequest = new ProvisionRequest(serviceInstanceGuid, plan: new Plan())
        lapiServiceProvider.provision(provisionRequest)

        when:
        def serviceInstance = new ServiceInstance(serviceInstanceGuid)
        def serviceBindingId = "serviceBindingId"
        def bindRequest = new BindRequest(serviceInstance: serviceInstance, parameters: ["serviceBindingId": serviceBindingId])
        lapiServiceProvider.bind(bindRequest)

        then:
        noExceptionThrown()
    }
}
