package com.swisscom.cloud.sb.broker.cfextensions.serviceusage

import com.google.common.base.Optional
import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.BindResponse
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.UpdateRequest
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.provisioning.ProvisionResponse
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.updating.UpdateResponse
import com.swisscom.cloud.sb.model.usage.ServiceUsage
import com.swisscom.cloud.sb.model.usage.ServiceUsageType
import spock.lang.Specification
import sun.reflect.generics.reflectiveObjects.NotImplementedException

class ServiceUsageLookupSpec extends Specification {
    ServiceProviderLookup serviceProviderLookup
    ServiceUsageLookup serviceUsageLookup

    def setup() {
        serviceProviderLookup = Mock(ServiceProviderLookup)
        serviceUsageLookup = new ServiceUsageLookup(serviceProviderLookup)
    }

    def "throws an exception when discovered serviceProvider does not provide usage info"() {
        given:
        serviceProviderLookup.findServiceProvider(_) >> Stub(ServiceProvider)
        when:
        serviceUsageLookup.usage(new ServiceInstance())
        then:
        def ex = thrown(RuntimeException)
        ex
    }

    def "happy path: usage info dto is created correctly"() {
        given:
        def service = new DummyService("1", ServiceUsageType.TRANSACTIONS)
        serviceProviderLookup.findServiceProvider(_) >> service

        when:
        ServiceUsage serviceUsage = serviceUsageLookup.usage(new ServiceInstance(), Optional.absent())

        then:
        serviceUsage.value == service.serviceUsageValue
        serviceUsage.type == service.serviceUsageType
    }

    private class DummyService implements ServiceProvider, ServiceUsageProvider {

        private String serviceUsageValue
        private ServiceUsageType serviceUsageType

        DummyService(String serviceUsageValue, ServiceUsageType serviceUsageType) {
            this.serviceUsageValue = serviceUsageValue
            this.serviceUsageType = serviceUsageType
        }

        @Override
        ServiceUsage findUsage(ServiceInstance serviceInstance, Optional<Date> enddate) {
            return new ServiceUsage(type: serviceUsageType, value: serviceUsageValue)
        }

        String getServiceUsageValue() {
            return serviceUsageValue
        }

        ServiceUsageType getServiceUsageType() {
            return serviceUsageType
        }

        @Override
        BindResponse bind(BindRequest request) {
            return null
        }

        @Override
        void unbind(UnbindRequest request) {

        }

        @Override
        UpdateResponse update(UpdateRequest request) {
            ErrorCode.SERVICE_UPDATE_NOT_ALLOWED.throwNew()
            return null
        }

        @Override
        ProvisionResponse provision(ProvisionRequest request) {
            return null
        }

        @Override
        DeprovisionResponse deprovision(DeprovisionRequest request) {
            return null
        }
    }
}
