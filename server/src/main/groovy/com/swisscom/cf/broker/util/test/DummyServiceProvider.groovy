package com.swisscom.cf.broker.util.test

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Optional
import com.swisscom.cf.broker.async.AsyncProvisioningService
import com.swisscom.cf.broker.binding.BindRequest
import com.swisscom.cf.broker.binding.BindResponse
import com.swisscom.cf.broker.binding.UnbindRequest
import com.swisscom.cf.broker.cfextensions.endpoint.EndpointProvider
import com.swisscom.cf.broker.provisioning.DeprovisionResponse
import com.swisscom.cf.broker.provisioning.ProvisionResponse
import com.swisscom.cf.broker.provisioning.job.ServiceDeprovisioningJob
import com.swisscom.cf.broker.provisioning.job.ServiceProvisioningJob
import com.swisscom.cf.broker.provisioning.job.DeprovisioningJobConfig
import com.swisscom.cf.broker.provisioning.job.ProvisioningjobConfig
import com.swisscom.cf.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cf.servicebroker.model.endpoint.Endpoint
import com.swisscom.cf.broker.model.*
import com.swisscom.cf.broker.provisioning.ProvisioningPersistenceService
import com.swisscom.cf.broker.services.common.*
import com.swisscom.cf.broker.provisioning.async.AsyncOperationResult
import com.swisscom.cf.broker.provisioning.async.AsyncServiceDeprovisioner
import com.swisscom.cf.broker.provisioning.async.AsyncServiceProvisioner
import com.swisscom.cf.broker.util.ServiceDetailKey
import com.swisscom.cf.broker.util.ServiceDetailsHelper
import groovy.util.logging.Slf4j
import org.apache.commons.lang.NotImplementedException
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Slf4j
class DummyServiceProvider implements ServiceProvider, AsyncServiceProvisioner, AsyncServiceDeprovisioner, EndpointProvider {
    public static final int RETRY_INTERVAL_IN_SECONDS = 10
    public static final int DEFAULT_PROCESSING_DELAY_IN_SECONDS = RETRY_INTERVAL_IN_SECONDS * 2

    @Autowired
    protected AsyncProvisioningService asyncProvisioningService


    @Autowired
    protected ProvisioningPersistenceService provisioningPersistenceService

    @Override
    BindResponse bind(BindRequest request) {
        throw new NotImplementedException()
    }

    @Override
    void unbind(UnbindRequest request) {
        throw new NotImplementedException()
    }

    @Override
    ProvisionResponse provision(ProvisionRequest request) {
        if (request.acceptsIncomplete) {
            provisioningPersistenceService.createServiceInstance(request, new ProvisionResponse(isAsync: true))
            asyncProvisioningService.scheduleProvision(new ProvisioningjobConfig(ServiceProvisioningJob.class, request, RETRY_INTERVAL_IN_SECONDS, 5))
            return new ProvisionResponse(details: [], isAsync: true)
        } else {
            return new ProvisionResponse(details: [], isAsync: false)
        }
    }

    @Override
    DeprovisionResponse deprovision(DeprovisionRequest request) {
        if (request.acceptsIncomplete) {
            asyncProvisioningService.scheduleDeprovision(new DeprovisioningJobConfig(ServiceDeprovisioningJob.class, request, RETRY_INTERVAL_IN_SECONDS, 5))
            return new DeprovisionResponse(isAsync: true)
        } else {
            return new DeprovisionResponse(isAsync: false)
        }
    }

    @Override
    AsyncOperationResult requestProvision(LastOperationJobContext context) {
        return processOperationResultBasedOnIfEnoughTimeHasElapsed(context, DEFAULT_PROCESSING_DELAY_IN_SECONDS)
    }

    private AsyncOperationResult processOperationResultBasedOnIfEnoughTimeHasElapsed(LastOperationJobContext context, int delay) {
        if (context.provisionRequest?.parameters) {
            Map<String, Object> params = new ObjectMapper().readValue(context.provisionRequest.parameters, new TypeReference<Map<String, Object>>() {
            })
            if (params.containsKey('success') && !params.get('success')) {
                return new AsyncOperationResult(status: LastOperation.Status.FAILED,
                        details: [ServiceDetail.from(ServiceDetailKey.DELAY_IN_SECONDS, String.valueOf(delay))])
            }
        }

        if (isServiceReady(context.lastOperation.dateCreation, delay)) {
            return new AsyncOperationResult(status: LastOperation.Status.SUCCESS,
                    details: [ServiceDetail.from(ServiceDetailKey.DELAY_IN_SECONDS, String.valueOf(delay))])
        } else {
            return new AsyncOperationResult(status: LastOperation.Status.IN_PROGRESS)
        }
    }

    @Override
    Optional<AsyncOperationResult> requestDeprovision(LastOperationJobContext context) {
        int delay = ServiceDetailsHelper.from(context.serviceInstance.details).getValue(ServiceDetailKey.DELAY_IN_SECONDS) as int
        return Optional.of(processOperationResultBasedOnIfEnoughTimeHasElapsed(context, delay))
    }

    private boolean isServiceReady(Date dateCreation, int delayInSeconds) {
        new DateTime(dateCreation).plusSeconds(delayInSeconds).isBeforeNow()
    }

    @Override
    Collection<Endpoint> findEndpoints(ServiceInstance serviceInstance) {
        return [new Endpoint(protocol: 'tcp', destination: '127.0.0.1', ports: '666')]
    }
}
