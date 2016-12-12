package com.swisscom.cf.broker.util

import com.google.common.collect.Sets
import com.swisscom.cf.broker.model.*
import com.swisscom.cf.broker.model.repository.*
import com.swisscom.cf.servicebroker.client.ServiceBrokerClient
import com.swisscom.cf.servicebroker.client.model.DeleteServiceInstanceBindingRequest
import com.swisscom.cf.servicebroker.client.model.DeleteServiceInstanceRequest
import com.swisscom.cf.servicebroker.client.model.LastOperationResponse
import groovy.transform.CompileStatic
import org.joda.time.LocalTime
import org.joda.time.Seconds
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest
import org.springframework.context.annotation.Scope
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
@Scope('prototype')
@CompileStatic
public class ServiceLifeCycler {
    private CFService cfService
    private Plan plan
    private PlanMetadata planMetaData

    private boolean serviceCreated
    private boolean planCreated

    private final String serviceInstanceId = UUID.randomUUID().toString()
    private final String serviceBindingId = UUID.randomUUID().toString()


    @Autowired
    private CFServiceRepository cfServiceRepository

    @Autowired
    private PlanRepository planRepository

    @Autowired
    private PlanMetadataRepository planMetadataRepository

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository

    @Autowired
    private ServiceBindingRepository serviceBindingRepository

    @Autowired
    private TagRepository tagRepository

    @Autowired
    private ParameterRepository parameterRepository

    private Map<String, Object> credentials

    void createServiceIfDoesNotExist(String serviceName, String serviceInternalName, String templateName = null,
                                     String planName = null, int maxBackups = 0) {
        cfService = cfServiceRepository.findByName(serviceName)
        if (cfService == null) {
            def tag =tagRepository.save( new Tag(tag: 'tag1'))
            cfService = cfServiceRepository.saveAndFlush(new CFService(guid: UUID.randomUUID().toString(),
                    name: serviceName, internalName: serviceInternalName,
                    description: "functional test", bindable: true, tags: Sets.newHashSet(tag)))
            serviceCreated = true
        }
        if (cfService.plans.empty) {
            plan = planRepository.saveAndFlush(new Plan(name: planName ?: 'plan', description: 'Plan for ' + serviceName,
                    guid: UUID.randomUUID().toString(), service: cfService,
                    templateUniqueIdentifier: templateName, maxBackups: maxBackups))
            planMetaData = planMetadataRepository.saveAndFlush(new PlanMetadata(key: 'key1', value: 'value1', plan: plan))
            plan.metadata.add(planMetaData)
            plan = planRepository.saveAndFlush(plan)
            cfService.plans.add(plan)
            cfServiceRepository.saveAndFlush(cfService)
            planCreated = true
        } else {
            if (planName) {
                plan = cfService.plans.find { it.name == planName }
            } else {
                plan = cfService.plans.first()
            }
        }

        //If the plan already exists make sure the maxBackups is set to the correct value
        if (plan.maxBackups != maxBackups) {
            plan.maxBackups = maxBackups
            planRepository.saveAndFlush(plan)
        }
    }



    void cleanup() {
        serviceInstanceRepository.deleteByGuid(serviceInstanceId)

        if (serviceCreated) {
            deletePlan()
            cfServiceRepository.delete(cfService)
        } else if (planCreated) {
            deletePlan()
        }
    }

    private void deletePlan() {
        plan.metadata.remove(planMetaData)
        cfService.plans.remove(plan)
        cfService = cfServiceRepository.saveAndFlush(cfService)
        planRepository.delete(plan)
    }


    void createServiceInstanceAndServiceBindingAndAssert(int delayInSecondsBetweenProvisionAndBind = 0,
                                                         boolean asyncRequest = false, boolean asyncResponse = false) {
        createServiceInstanceAndAssert(asyncRequest, asyncResponse)

        pauseExecution(delayInSecondsBetweenProvisionAndBind)

        bindServiceInstanceAndAssert()
        println("Created serviceInstanceId:${serviceInstanceId} , serviceBindingId ${serviceBindingId}")
    }

    void createServiceInstanceAndAssert(boolean asyncRequest = false, boolean asyncResponse = false,
                                        Map<String, Object> provisionParameters = null) {
        ResponseEntity provisionResponse = requestServiceProvisioning(asyncRequest, provisionParameters)
        if (asyncResponse) {
            assert provisionResponse.statusCode == HttpStatus.ACCEPTED
        } else {
            assert provisionResponse.statusCode == HttpStatus.CREATED
        }
    }

    ResponseEntity requestServiceProvisioning(boolean async, Map<String, Object> parameters) {
        def request = new CreateServiceInstanceRequest(cfService.guid, plan.guid, 'org_id', 'space_id', parameters)
        return createServiceBrokerClient().createServiceInstance(request.withServiceInstanceId(serviceInstanceId).withAsyncAccepted(async))
    }

    Map<String, Object> bindServiceInstanceAndAssert(String bindingId = null, Map bindingParameters = null, boolean uniqueCredentials = true) {
        def bindResponse = requestBindService(bindingId, bindingParameters)
        assert bindResponse.statusCode == (uniqueCredentials ? HttpStatus.CREATED : HttpStatus.OK)
        credentials = bindResponse.body.credentials
        return bindResponse.body.credentials
    }

    ResponseEntity<CreateServiceInstanceAppBindingResponse> requestBindService(String bindingId = null, Map bindingParameters = null) {
        if (!bindingId) {
            bindingId = serviceBindingId
        }

        def request = new CreateServiceInstanceBindingRequest(cfService.guid, plan.guid, 'app_guid', null, bindingParameters)

        return createServiceBrokerClient().createServiceInstanceBinding(request.withServiceInstanceId(serviceInstanceId)
                .withBindingId(serviceBindingId))
    }

    void deleteServiceBindingAndServiceInstaceAndAssert(boolean isAsync = false) {
        deleteServiceBindingAndAssert()
        deleteServiceInstanceAndAssert(isAsync)
    }

    void deleteServiceInstanceAndAssert(boolean isAsync = false) {
        def deprovisionResponse = createServiceBrokerClient().deleteServiceInstance(new DeleteServiceInstanceRequest(serviceInstanceId,
                cfService.guid, plan.guid, isAsync))

        if (isAsync) {
            assert deprovisionResponse.statusCode == HttpStatus.ACCEPTED
        } else {
            assert deprovisionResponse.statusCode == HttpStatus.OK
            assert !serviceBindingRepository.findByGuid(serviceBindingId)
            assert serviceInstanceRepository.findByGuid(serviceInstanceId).deleted
        }
    }

    void deleteServiceBindingAndAssert(String bindingId = null) {
        if (!bindingId) {
            bindingId = serviceBindingId
        }

        ResponseEntity unbindResponse = createServiceBrokerClient().deleteServiceInstanceBinding(new DeleteServiceInstanceBindingRequest(serviceInstanceId,
                bindingId, cfService.guid, plan.guid))
        assert unbindResponse.statusCode == HttpStatus.OK
    }

    LastOperationResponse getServiceInstanceStatus() {
        return createServiceBrokerClient().getServiceInstanceLastOperation(serviceInstanceId).body
    }

    Parameter createParameter(String name, String value, Plan plan) {
        return parameterRepository.save(new Parameter(name: name, value: value, plan: plan))
    }

    CFService getCfService() {
        return cfService
    }

    Plan getPlan() {
        return plan
    }

    String getServiceBindingId() {
        return serviceBindingId
    }

    String getServiceInstanceId() {
        return serviceInstanceId
    }

    private ServiceBrokerClient createServiceBrokerClient() {
        return new ServiceBrokerClient('http://localhost:8080', null, null)
    }

    public static def pauseExecution(int seconds) {
        if (seconds > 0) {

            for (def start = LocalTime.now(); start.plusSeconds(seconds).isAfter(LocalTime.now()); Thread.sleep(1000)) {
                println("Execution continues in ${Seconds.secondsBetween(LocalTime.now(), start.plusSeconds(seconds)).getSeconds()} second(s)")
            }
        }
    }

    Map<String, Object> getCredentials() {
        return credentials
    }
}