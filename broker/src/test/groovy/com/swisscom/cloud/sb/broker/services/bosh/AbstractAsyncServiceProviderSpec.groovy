package com.swisscom.cloud.sb.broker.services.bosh

import com.swisscom.cloud.sb.broker.async.AsyncProvisioningService
import com.swisscom.cloud.sb.broker.cfextensions.endpoint.EndpointLookup
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.provisioning.ProvisioningPersistenceService
import com.swisscom.cloud.sb.broker.provisioning.job.ServiceProvisioningJob
import com.swisscom.cloud.sb.broker.services.AsyncServiceProvider
import com.swisscom.cloud.sb.broker.util.test.ErrorCodeHelper
import spock.lang.Specification

import java.lang.reflect.ParameterizedType

abstract class AbstractAsyncServiceProviderSpec<T extends AsyncServiceProvider> extends Specification {
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
