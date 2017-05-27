package com.swisscom.cf.broker.services.bosh

import com.swisscom.cf.broker.async.AsyncProvisioningService
import com.swisscom.cf.broker.cfextensions.endpoint.EndpointProvider
import com.swisscom.cf.broker.provisioning.DeprovisionResponse
import com.swisscom.cf.broker.provisioning.ProvisionResponse
import com.swisscom.cf.broker.provisioning.job.ServiceDeprovisioningJob
import com.swisscom.cf.broker.provisioning.job.ServiceProvisioningJob
import com.swisscom.cf.broker.provisioning.job.DeprovisioningJobConfig
import com.swisscom.cf.broker.provisioning.job.ProvisioningjobConfig
import com.swisscom.cloud.servicebroker.model.endpoint.Endpoint
import com.swisscom.cf.broker.model.DeprovisionRequest
import com.swisscom.cf.broker.model.ProvisionRequest
import com.swisscom.cf.broker.model.ServiceInstance
import com.swisscom.cf.broker.provisioning.ProvisioningPersistenceService
import com.swisscom.cf.broker.services.common.*
import com.swisscom.cf.broker.provisioning.async.AsyncServiceDeprovisioner
import com.swisscom.cf.broker.provisioning.async.AsyncServiceProvisioner
import com.swisscom.cf.broker.cfextensions.endpoint.EndpointLookup
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired

@CompileStatic
@Slf4j
abstract class BoshBasedServiceProvider<T extends BoshBasedServiceConfig> implements ServiceProvider, AsyncServiceProvisioner, AsyncServiceDeprovisioner, EndpointProvider, BoshTemplateCustomizer {

    @Autowired
    protected AsyncProvisioningService asyncProvisioningService
    @Autowired
    protected ProvisioningPersistenceService provisioningPersistenceService
    @Autowired
    protected T serviceConfig
    @Autowired
    protected EndpointLookup endpointLookup
    @Autowired
    protected BoshFacadeFactory boshFacadeFactory

    @Override
    ProvisionResponse provision(ProvisionRequest request) {
        Utils.verifyAsychronousCapableClient(request)

        asyncProvisioningService.scheduleProvision(
                new ProvisioningjobConfig(ServiceProvisioningJob.class, request,
                        serviceConfig.retryIntervalInSeconds,
                        serviceConfig.maxRetryDurationInMinutes))
        return new ProvisionResponse(isAsync: true)
    }

    @Override
    DeprovisionResponse deprovision(DeprovisionRequest request) {
        asyncProvisioningService.scheduleDeprovision(new DeprovisioningJobConfig(ServiceDeprovisioningJob.class, request,
                serviceConfig.retryIntervalInSeconds, serviceConfig.maxRetryDurationInMinutes))
        return new DeprovisionResponse(isAsync: true)
    }

    @Override
    Collection<Endpoint> findEndpoints(ServiceInstance serviceInstance) {
        return endpointLookup.findEndpoints(serviceInstance, serviceConfig)
    }

    BoshFacade getBoshFacade() {
        return boshFacadeFactory.build(serviceConfig)
    }
}
