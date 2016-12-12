package com.swisscom.cf.broker.services.bosh

import com.swisscom.cf.broker.async.AsyncProvisioningService
import com.swisscom.cf.broker.async.job.ServiceDeprovisioningJob
import com.swisscom.cf.broker.async.job.ServiceProvisioningJob
import com.swisscom.cf.broker.async.job.config.DeprovisioningJobConfig
import com.swisscom.cf.broker.async.job.config.ProvisioningjobConfig
import com.swisscom.cf.broker.filterextensions.endpoint.EndpointDto
import com.swisscom.cf.broker.model.DeprovisionRequest
import com.swisscom.cf.broker.model.ProvisionRequest
import com.swisscom.cf.broker.model.ServiceInstance
import com.swisscom.cf.broker.provisioning.ProvisioningPersistenceService
import com.swisscom.cf.broker.services.common.*
import com.swisscom.cf.broker.services.common.async.AsyncServiceDeprovisioner
import com.swisscom.cf.broker.services.common.async.AsyncServiceProvisioner
import com.swisscom.cf.broker.services.common.endpoint.EndpointDtoGenerator
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired

@CompileStatic
@Log4j
abstract class BoshBasedServiceProvider<T extends BoshBasedServiceConfig> implements ServiceProvider, AsyncServiceProvisioner, AsyncServiceDeprovisioner, EndpointProvider, BoshTemplateCustomizer {

    @Autowired
    protected AsyncProvisioningService asyncProvisioningService
    @Autowired
    protected ProvisioningPersistenceService provisioningPersistenceService
    @Autowired
    protected T serviceConfig
    @Autowired
    protected EndpointDtoGenerator endpointDtoGenerator
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
    Collection<EndpointDto> findEndpoints(ServiceInstance serviceInstance) {
        return endpointDtoGenerator.findEndpoints(serviceInstance, serviceConfig)
    }

    BoshFacade getBoshFacade() {
        return boshFacadeFactory.build(serviceConfig)
    }
}
