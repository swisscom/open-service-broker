package com.swisscom.cloud.sb.broker.provisioning

import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.services.openwhisk.OpenWhiskConfig
import com.swisscom.cloud.sb.broker.services.openwhisk.OpenWhiskDbClient
import com.swisscom.cloud.sb.broker.services.openwhisk.OpenWhiskServiceProvider
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import org.springframework.context.ApplicationContext
import spock.lang.Specification

class ServiceProviderLookupSpec extends Specification {

    private ServiceProviderLookup serviceProviderLookup
    private OpenWhiskServiceProvider openWhiskServiceProvider
    private ServiceInstanceRepository serviceInstanceRepository
    private ServiceBindingRepository serviceBindingRepository
    private ApplicationContext applicationContext
    private Plan plan

    void setup() {
        plan = new Plan()
        applicationContext = Mock(ApplicationContext)
        serviceProviderLookup = new ServiceProviderLookup(appContext: applicationContext)
        openWhiskServiceProvider = new OpenWhiskServiceProvider(new OpenWhiskConfig(), new OpenWhiskDbClient(new OpenWhiskConfig(), new RestTemplateBuilder()), serviceInstanceRepository, serviceBindingRepository)
    }

    def "find service provider by plan.serviceProviderName"() {
        given:
        plan.serviceProviderClass = "openWhiskServiceProvider"
        plan.internalName = null
        plan.service = null
        applicationContext.getBean("openWhiskServiceProvider") >> openWhiskServiceProvider

        when:
        serviceProviderLookup.findServiceProvider(plan) == openWhiskServiceProvider

        then:
        noExceptionThrown()
    }

    def "find service provider by plan.internalName"() {
        given:
        plan.serviceProviderClass = null
        plan.internalName = "openWhisk"
        plan.service = null
        serviceProviderLookup.findServiceProvider(plan) >> openWhiskServiceProvider

        when:
        serviceProviderLookup.findServiceProvider(plan) == openWhiskServiceProvider

        then:
        noExceptionThrown()
    }

    def "find service provider by plan.service.serviceProviderName"() {
        given:
        plan.serviceProviderClass = null
        plan.internalName = null
        plan.service = new CFService(serviceProviderClass: "openWhiskServiceProvider")
        serviceProviderLookup.findServiceProvider(plan) >> openWhiskServiceProvider

        when:
        serviceProviderLookup.findServiceProvider(plan) == openWhiskServiceProvider

        then:
        noExceptionThrown()
    }

    def "find service provider by plan.service.internalName"() {
        given:
        plan.serviceProviderClass = null
        plan.internalName = null
        plan.service = new CFService(internalName: "openWhisk")
        serviceProviderLookup.findServiceProvider(plan) >> openWhiskServiceProvider

        when:
        serviceProviderLookup.findServiceProvider(plan) == openWhiskServiceProvider

        then:
        noExceptionThrown()
    }
}
