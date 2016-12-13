package com.swisscom.cf.broker.services.bosh

import com.swisscom.cf.broker.async.AsyncProvisioningService
import com.swisscom.cf.broker.async.job.ServiceProvisioningJob
import com.swisscom.cf.broker.exception.ErrorCode
import com.swisscom.cf.broker.exception.ServiceBrokerException
import com.swisscom.cf.broker.model.ProvisionRequest
import com.swisscom.cf.broker.provisioning.ProvisioningPersistenceService
import com.swisscom.cf.broker.cfextensions.endpoint.EndpointLookup
import com.swisscom.cf.broker.util.test.ErrorCodeHelper
import spock.lang.Specification

import java.lang.reflect.ParameterizedType

abstract class AbstractBoshBasedServiceProviderSpec<T extends BoshBasedServiceProvider> extends Specification {
    public static final String serviceInstanceGuid = "serviceInstanceGuid"

    T serviceProvider
    BoshFacade boshFacade

    void setup() {
        serviceProvider = ((Class) ((ParameterizedType) this.getClass().
                getGenericSuperclass()).getActualTypeArguments()[0]).newInstance()
        serviceProvider.asyncProvisioningService = Mock(AsyncProvisioningService)
        serviceProvider.provisioningPersistenceService = Mock(ProvisioningPersistenceService)
        serviceProvider.endpointLookup = Mock(EndpointLookup)
        serviceProvider.serviceConfig = new DummyConfig(retryIntervalInSeconds: 1, maxRetryDurationInMinutes: 1)
        and:
        boshFacade = Mock(BoshFacade)
        serviceProvider.boshFacadeFactory = Mock(BoshFacadeFactory) {
            build(_) >> boshFacade
        }
    }

    def "synchronous provisioning requests are not allowed"() {
        when:
        serviceProvider.provision(new ProvisionRequest(acceptsIncomplete: false))
        then:
        def ex = thrown(ServiceBrokerException)
        ErrorCodeHelper.assertServiceBrokerException(ex, ErrorCode.ASYNC_REQUIRED)
    }

    def "provisioning job scheduling works correctly"() {
        given:
        def serviceInstaceGuid = 'serviceInstanceGuid'
        def request = new ProvisionRequest(acceptsIncomplete: true, serviceInstanceGuid: serviceInstaceGuid)
        when:
        def result = serviceProvider.provision(request)
        then:
        result.isAsync
        1 * serviceProvider.asyncProvisioningService.scheduleProvision({
            it.jobClass == ServiceProvisioningJob.class &&
                    it.guid == serviceInstaceGuid && it.retryIntervalInSeconds == serviceProvider.serviceConfig.retryIntervalInSeconds && it.maxRetryDurationInMinutes == serviceProvider.serviceConfig.maxRetryDurationInMinutes
        })
    }


}
