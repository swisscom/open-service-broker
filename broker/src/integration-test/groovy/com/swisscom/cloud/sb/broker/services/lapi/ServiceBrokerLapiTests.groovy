package com.swisscom.cloud.sb.broker.services.lapi

import com.swisscom.cloud.sb.broker.BaseTransactionalSpecification
import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder

class ServiceBrokerLapiTests extends BaseTransactionalSpecification {

    private LapiServiceProvider lapiServiceProvider
    private String SERVICE_INSTANCE_GUID = "65d546f1-2c74-4871-9d5f-b5b0df1a8912"

    def "setup"() {
        def restTemplateBuilder = new RestTemplateBuilder()
        lapiServiceProvider = new LapiServiceProvider(restTemplateBuilder)
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

    }
}
