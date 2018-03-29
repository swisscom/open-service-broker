package com.swisscom.cloud.sb.broker.util.test

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Optional
import com.google.gson.Gson
import com.swisscom.cloud.sb.broker.async.AsyncProvisioningService
import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.BindResponse
import com.swisscom.cloud.sb.broker.binding.BindResponseDto
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.cfextensions.endpoint.EndpointProvider
import com.swisscom.cloud.sb.broker.model.*
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.provisioning.ProvisionResponse
import com.swisscom.cloud.sb.broker.provisioning.ProvisioningPersistenceService
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncOperationResult
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncServiceDeprovisioner
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncServiceProvisioner
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncServiceUpdater
import com.swisscom.cloud.sb.broker.provisioning.job.DeprovisioningJobConfig
import com.swisscom.cloud.sb.broker.provisioning.job.ProvisioningJobConfig
import com.swisscom.cloud.sb.broker.provisioning.job.ServiceDeprovisioningJob
import com.swisscom.cloud.sb.broker.provisioning.job.ServiceProvisioningJob
import com.swisscom.cloud.sb.broker.provisioning.job.ServiceUpdateJob
import com.swisscom.cloud.sb.broker.provisioning.job.UpdateJobConfig
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import com.swisscom.cloud.sb.broker.updating.UpdatableProvisioner
import com.swisscom.cloud.sb.broker.updating.UpdateResponse
import com.swisscom.cloud.sb.broker.util.servicedetail.AbstractServiceDetailKey
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailType
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper
import com.swisscom.cloud.sb.model.endpoint.Endpoint
import groovy.util.logging.Slf4j
import org.apache.commons.lang.NotImplementedException
import org.apache.commons.lang.StringUtils
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Slf4j
class DummyServiceProvider implements ServiceProvider, AsyncServiceProvisioner, AsyncServiceDeprovisioner, AsyncServiceUpdater, EndpointProvider, UpdatableProvisioner {
    public static final int RETRY_INTERVAL_IN_SECONDS = 10
    public static final int DEFAULT_PROCESSING_DELAY_IN_SECONDS = RETRY_INTERVAL_IN_SECONDS * 2

    @Autowired
    protected AsyncProvisioningService asyncProvisioningService


    @Autowired
    protected ProvisioningPersistenceService provisioningPersistenceService

    @Override
    BindResponse bind(BindRequest request) {
        log.info("Bind parameters: ${request.parameters?.toString()}")
        return new BindResponse(credentials: new BindResponseDto() {
            @Override
            String toJson() {
                request.parameters ? new Gson().toJson(request.parameters) : '{}'
            }
        })
    }

    @Override
    void unbind(UnbindRequest request) {

    }

    private static DummyServiceProviderParameters DeserializeParameters(String jsonParameters) {
        return new ObjectMapper().readValue(jsonParameters, DummyServiceProviderParameters.class)
    }

    @Override
    ProvisionResponse provision(ProvisionRequest request) {
        def serviceDetails = new ArrayList<ServiceDetail>()
        if (!StringUtils.isEmpty(request.parameters)) {
            serviceDetails.add(new ServiceDetail(
                    key: "mode",
                    uniqueKey: true,
                    value: DeserializeParameters(request.parameters).mode))
        }

        if (request.acceptsIncomplete) {
            asyncProvisioningService.scheduleProvision(new ProvisioningJobConfig(ServiceProvisioningJob.class, request, RETRY_INTERVAL_IN_SECONDS, 5))
            return new ProvisionResponse(details: serviceDetails, isAsync: true)
        } else {
            return new ProvisionResponse(details: serviceDetails, isAsync: false)
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

    protected AsyncOperationResult processOperationResultBasedOnIfEnoughTimeHasElapsed(LastOperationJobContext context, int delay) {
        def serviceDetails = [ServiceDetail.from(DummyServiceProviderServiceDetailKey.DELAY_IN_SECONDS, String.valueOf(delay))]
        if (context.provisionRequest?.parameters) {
            Map<String, Object> params = new ObjectMapper().readValue(context.provisionRequest.parameters, new TypeReference<Map<String, Object>>() {
            })
            if (params.containsKey('success') && !params.get('success')) {
                return new AsyncOperationResult(status: LastOperation.Status.FAILED, details: serviceDetails)
            }
        }

        if (isServiceReady(context.lastOperation.dateCreation, delay)) {
            return new AsyncOperationResult(status: LastOperation.Status.SUCCESS, details: serviceDetails)
        } else {
            return new AsyncOperationResult(status: LastOperation.Status.IN_PROGRESS, details: serviceDetails)
        }
    }

    @Override
    Optional<AsyncOperationResult> requestDeprovision(LastOperationJobContext context) {
        def serviceDetails = ServiceDetailsHelper.from(context.serviceInstance.details)
        int delay = serviceDetails.details.size() != 0 ?
                serviceDetails.getValue(DummyServiceProviderServiceDetailKey.DELAY_IN_SECONDS) as int :
                DEFAULT_PROCESSING_DELAY_IN_SECONDS
        return Optional.of(processOperationResultBasedOnIfEnoughTimeHasElapsed(context, delay))
    }

    private static boolean isServiceReady(Date dateCreation, int delayInSeconds) {
        new DateTime(dateCreation).plusSeconds(delayInSeconds).isBeforeNow()
    }

    @Override
    Collection<Endpoint> findEndpoints(ServiceInstance serviceInstance) {
        return [new Endpoint(protocol: 'tcp', destination: '127.0.0.1', ports: '666')]
    }

    @Override
    UpdateResponse updateParameters(UpdateRequest updateRequest) {
        def serviceDetails = new ArrayList<ServiceDetail>()
        if (!StringUtils.isEmpty(updateRequest.parameters)) {
            serviceDetails.add(new ServiceDetail(
                        key: "mode",
                        uniqueKey: true,
                        value: DeserializeParameters(updateRequest.parameters).mode))
        }

        if (updateRequest.acceptsIncomplete) {
            asyncProvisioningService.scheduleUpdate(new UpdateJobConfig(ServiceUpdateJob.class, updateRequest, updateRequest.serviceInstanceGuid, RETRY_INTERVAL_IN_SECONDS, 5))
            return new UpdateResponse(isAsync: true, details: serviceDetails)
        } else {
            return new UpdateResponse(isAsync: false, details: serviceDetails)
        }
    }

    @Override
    UpdateResponse updatePlanAndParameters(UpdateRequest updateRequest) {
        return updateParameters(updateRequest)
    }

    @Override
    AsyncOperationResult requestUpdate(LastOperationJobContext context) {
        return processOperationResultBasedOnIfEnoughTimeHasElapsed(context, DEFAULT_PROCESSING_DELAY_IN_SECONDS)
    }

    enum DummyServiceProviderServiceDetailKey implements AbstractServiceDetailKey {
        DELAY_IN_SECONDS("delay_in_seconds", ServiceDetailType.OTHER)

        DummyServiceProviderServiceDetailKey(String key, ServiceDetailType serviceDetailType) {
            com_swisscom_cloud_sb_broker_util_servicedetail_AbstractServiceDetailKey__key = key
            com_swisscom_cloud_sb_broker_util_servicedetail_AbstractServiceDetailKey__serviceDetailType = serviceDetailType
        }
    }
}
