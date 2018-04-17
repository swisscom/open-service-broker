package com.swisscom.cloud.sb.broker.util

import com.google.common.collect.Sets
import com.swisscom.cloud.sb.broker.config.ApplicationUserConfig
import com.swisscom.cloud.sb.broker.config.UserConfig
import com.swisscom.cloud.sb.broker.config.WebSecurityConfig
import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Parameter
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.PlanMetadata
import com.swisscom.cloud.sb.broker.model.Tag
import com.swisscom.cloud.sb.broker.model.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.model.repository.ParameterRepository
import com.swisscom.cloud.sb.broker.model.repository.PlanMetadataRepository
import com.swisscom.cloud.sb.broker.model.repository.PlanRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.model.repository.TagRepository
import com.swisscom.cloud.sb.client.ServiceBrokerClient
import com.swisscom.cloud.sb.client.model.DeleteServiceInstanceBindingRequest
import com.swisscom.cloud.sb.client.model.DeleteServiceInstanceRequest
import com.swisscom.cloud.sb.client.model.LastOperationResponse
import com.swisscom.cloud.sb.client.model.LastOperationState

import com.swisscom.cloud.sb.client.model.ProvisionResponseDto
import groovy.transform.CompileStatic
import org.joda.time.LocalTime
import org.joda.time.Seconds
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.BindResource
import org.springframework.cloud.servicebroker.model.Context
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceRequest
import org.springframework.context.annotation.Scope
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate

import javax.annotation.PostConstruct

@Component
@Scope('prototype')
@CompileStatic
class ServiceLifeCycler {
    private CFService cfService
    private Plan plan
    private Set<Plan> plans = []
    private PlanMetadata planMetaData
    private Parameter parameter
    private ArrayList<Parameter> parameters = new ArrayList<Parameter>()
    private String backupId

    private boolean serviceCreated
    private boolean planCreated

    private Set<String> serviceInstanceIds = []
    private String serviceInstanceId
    private String serviceBindingId

    @Autowired
    private ApplicationUserConfig userConfig
    private UserConfig cfAdminUser
    private UserConfig cfExtUser

    ServiceLifeCycler() {
        this(UUID.randomUUID().toString(), UUID.randomUUID().toString())
    }

    ServiceLifeCycler(String serviceInstanceId) {
        this(serviceInstanceId, UUID.randomUUID().toString())
    }

    ServiceLifeCycler(String serviceInstanceId, String serviceBindingId) {
        this.serviceInstanceId = serviceInstanceId
        this.serviceBindingId = serviceBindingId
        this.serviceInstanceIds << serviceInstanceId
    }

    @PostConstruct
    private void init() {
        cfAdminUser = getUserByRole(WebSecurityConfig.ROLE_CF_ADMIN)
        cfExtUser = getUserByRole(WebSecurityConfig.ROLE_CF_EXT_ADMIN)
    }

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

    CFService createServiceIfDoesNotExist(String serviceName, String serviceInternalName, String templateName = null, String templateVersion = null,
                                     String planName = null, int maxBackups = 0, boolean instancesRetrievable = false, boolean bindingsRetrievable = false,
                                          String serviceInstanceCreateSchema = null, String serviceInstanceUpdateSchema = null, String serviceBindingCreateSchema = null, Plan servicePlan = null) {
        cfService = cfServiceRepository.findByName(serviceName)
        if (cfService == null) {
            def tag = tagRepository.saveAndFlush(new Tag(tag: 'tag1'))
            cfService = cfServiceRepository.saveAndFlush(new CFService(guid: UUID.randomUUID().toString(),
                    name: serviceName, internalName: serviceInternalName,
                    description: "functional test", bindable: true, tags: Sets.newHashSet(tag), instancesRetrievable: instancesRetrievable, bindingsRetrievable: bindingsRetrievable))
            serviceCreated = true
        }
        if (cfService.plans.empty && servicePlan == null) {
            plan = planRepository.saveAndFlush(new Plan(name: planName ?: 'plan', description: 'Plan for ' + serviceName,
                    guid: UUID.randomUUID().toString(), service: cfService,
                    templateUniqueIdentifier: templateName, templateVersion: templateVersion, maxBackups: maxBackups,
                    serviceInstanceCreateSchema: serviceInstanceCreateSchema,
                    serviceInstanceUpdateSchema: serviceInstanceUpdateSchema,
                    serviceBindingCreateSchema: serviceBindingCreateSchema
            ))
            planMetaData = planMetadataRepository.saveAndFlush(new PlanMetadata(key: 'key1', value: 'value1', plan: plan))
            plan.metadata.add(planMetaData)
            plan = planRepository.saveAndFlush(plan)
            setPlan(plan)
            cfService.plans.add(plan)
            cfServiceRepository.saveAndFlush(cfService)
            planCreated = true
        } else if (cfService.plans.empty && servicePlan != null) {
            plan = planRepository.saveAndFlush(new Plan(guid: servicePlan.guid, service:cfService, internalName: servicePlan.internalName, asyncRequired: servicePlan.asyncRequired))
            cfService.plans.add(plan)
            setPlan(plan)
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
        return cfService
    }

    void cleanup() {

        (serviceInstanceIds as String[]).reverseEach { it ->
            serviceInstanceRepository.deleteByGuid(it)
        }

        if (parameters.size() > 0) {
            parameters.each {
                (plans as Plan[]).reverseEach { planIt ->
                    planIt.parameters.remove(it)
                    planRepository.saveAndFlush(planIt)
                }
                parameterRepository.delete(it)
            }
        }

        def cfServices = cfServiceRepository.findAll()
        cfServices.each { it ->
            (plans as Plan[]).reverseEach { planIt ->
                it.plans.remove(planIt)
                it = cfServiceRepository.saveAndFlush(it)
            }
        }

        (plans as Plan[]).reverseEach { it ->
            deletePlan(it)
        }
    }

    private void deletePlan(Plan plan) {
        plan.metadata.remove(planMetaData)
        planRepository.delete(plan)
    }

    void createServiceInstanceAndServiceBindingAndAssert(int maxDelayInSecondsBetweenProvisionAndBind = 0,
                                                         boolean asyncRequest = false, boolean asyncResponse = false, String newServiceInstanceId = serviceInstanceId, Context context = null) {
        createServiceInstanceAndAssert(maxDelayInSecondsBetweenProvisionAndBind, asyncRequest, asyncResponse, null, context)
        bindServiceInstanceAndAssert(null, null, true, context)
        println("Created serviceInstanceId:${newServiceInstanceId} , serviceBindingId ${serviceBindingId}")
    }

    void createServiceBindingAndAssert(int maxDelayInSecondsBetweenProvisionAndBind = 0, boolean asyncRequest = false, boolean asyncResponse = false, Context context = null) {
        bindServiceInstanceAndAssert(null, null, true, context)
        println("Bound serviceInstanceId: ${serviceInstanceId} , serviceBindingId ${serviceBindingId}")
    }


    void createServiceInstanceAndAssert(int maxSecondsToAwaitInstance = 0, boolean asyncRequest = false, boolean asyncResponse = false, Map<String, Object> provisionParameters = null, Context context = null) {
        ResponseEntity provisionResponse = requestServiceProvisioning(asyncRequest, context, provisionParameters)
        if (asyncResponse) {
            assert provisionResponse.statusCode == HttpStatus.ACCEPTED
            waitUntilMaxTimeOrTargetState(maxSecondsToAwaitInstance)
        } else {
            assert provisionResponse.statusCode == HttpStatus.CREATED
        }
    }

    ResponseEntity<ProvisionResponseDto> provision(boolean async, Context context, Map<String, Object> parameters, boolean throwExceptionWhenNon2xxHttpStatusCode = true){
        def request = new CreateServiceInstanceRequest(cfService.guid, plan.guid, 'org_id', 'space_id', context, parameters)
        return createServiceBrokerClient(throwExceptionWhenNon2xxHttpStatusCode).provision(request.withServiceInstanceId(serviceInstanceId).withAsyncAccepted(async))
    }

    ResponseEntity requestServiceProvisioning(boolean async, Context context, Map<String, Object> parameters, boolean throwExceptionWhenNon2xxHttpStatusCode = true) {
        return requestServiceProvisioning(serviceInstanceId, cfService.guid, plan.guid, async, context, parameters, throwExceptionWhenNon2xxHttpStatusCode)
    }

    ResponseEntity requestServiceProvisioning(
            final String serviceInstanceId,
            final String serviceGuid,
            final String planGuid,
            boolean async,
            Context context,
            Map<String, Object> parameters,
            boolean throwExceptionWhenNon2xxHttpStatusCode = true) {
        def request = new CreateServiceInstanceRequest(serviceGuid, planGuid, 'org_id', 'space_id', context, parameters)
        return createServiceBrokerClient(throwExceptionWhenNon2xxHttpStatusCode)
                .createServiceInstance(request.withServiceInstanceId(serviceInstanceId).withAsyncAccepted(async))
    }

    Map<String, Object> bindServiceInstanceAndAssert(String bindingId = null, Map bindingParameters = null, boolean uniqueCredentials = true, Context context = null) {
        def bindResponse = requestBindService(bindingId, bindingParameters, context)
        assert bindResponse.statusCode == (uniqueCredentials ? HttpStatus.CREATED : HttpStatus.OK)
        credentials = bindResponse.body.credentials
        return bindResponse.body.credentials
    }

    ResponseEntity<CreateServiceInstanceAppBindingResponse> requestBindService(String bindingId = null, Map bindingParameters = null, Context context = null) {
        if (!bindingId) {
            bindingId = serviceBindingId
        }
        def bindResource = new BindResource('app-id', 'app-id.example.com', null)
        def request = new CreateServiceInstanceBindingRequest(cfService.guid, plan.guid, bindResource, context, bindingParameters)

        return createServiceBrokerClient().createServiceInstanceBinding(request.withServiceInstanceId(serviceInstanceId)
                .withBindingId(bindingId))
    }

    void deleteServiceBindingAndServiceInstanceAndAssert(boolean isAsync = false, int maxSecondsToAwaitDelete = 0) {
        deleteServiceBindingAndAssert()
        deleteServiceInstanceAndAssert(isAsync, maxSecondsToAwaitDelete)
    }

    void deleteServiceInstanceAndAssert(boolean isAsync = false, int maxSecondsToAwaitDelete = 0) {
        deleteServiceInstanceAndAssert(serviceInstanceId, cfService.guid, plan.guid, serviceBindingId, isAsync, maxSecondsToAwaitDelete)
    }

    void deleteServiceInstanceAndAssert(
            final String serviceInstanceId,
            final String serviceGuid,
            final String planGuid,
            final String serviceBindingId = null,
            boolean isAsync = false,
            int maxSecondsToAwaitDelete = 0) {
        def deprovisionResponse = createServiceBrokerClient().deleteServiceInstance(new DeleteServiceInstanceRequest(serviceInstanceId,
                serviceGuid, planGuid, isAsync))

        if (isAsync) {
            assert deprovisionResponse.statusCode == HttpStatus.ACCEPTED
            waitUntilMaxTimeOrTargetState(maxSecondsToAwaitDelete)
        } else {
            assert deprovisionResponse.statusCode == HttpStatus.OK
            if (serviceInstanceId != null)
                assert !serviceBindingRepository.findByGuid(serviceBindingId)
            assert serviceInstanceRepository.findByGuid(serviceInstanceId).deleted
        }
    }

    void deleteServiceBindingAndAssert(String bindingId = null, String serviceInstanceIdToBeUnbound = serviceInstanceId) {
        if (!bindingId) {
            bindingId = serviceBindingId
        }

        ResponseEntity unbindResponse = createServiceBrokerClient().deleteServiceInstanceBinding(new DeleteServiceInstanceBindingRequest(serviceInstanceIdToBeUnbound,
                bindingId, cfService.guid, plan.guid))
        assert unbindResponse.statusCode == HttpStatus.OK
    }

    LastOperationResponse getServiceInstanceStatus(String newServiceInstanceId = serviceInstanceId) {
        return createServiceBrokerClient().getServiceInstanceLastOperation(newServiceInstanceId).body
    }

    Parameter createParameter(String name, String value, Plan plan) {
        parameter = new Parameter(name: name, value: value, plan: plan)
        parameters.add(parameter)
        return parameterRepository.saveAndFlush(parameter)
    }

    void setAsyncRequestInPlan(boolean asyncRequired) {
        plan.asyncRequired = asyncRequired
        plan = planRepository.saveAndFlush(plan)
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

    void setBackupId(String id) {
        backupId = id
    }

    String getBackupId() {
        backupId
    }

    private ServiceBrokerClient createServiceBrokerClient(boolean throwExceptionWhenNon2xxHttpStatusCode = true) {
        if (throwExceptionWhenNon2xxHttpStatusCode) {
            return new ServiceBrokerClient('http://localhost:8080', cfAdminUser.username, cfAdminUser.password)
        } else {
            return createServiceBrokerClientWithCustomErrorHandler()
        }
    }

    private ServiceBrokerClient createServiceBrokerClientExternal() {
        return new ServiceBrokerClient('http://localhost:8080', cfExtUser.username, cfExtUser.password)
    }


    private ServiceBrokerClient createServiceBrokerClientWithCustomErrorHandler() {
        def template = new RestTemplate(new HttpComponentsClientHttpRequestFactory())
        template.errorHandler = new NoOpResponseErrorHandler()
        return new ServiceBrokerClient(template, 'http://localhost:8080', cfAdminUser.username, cfAdminUser.password)
    }

    static def pauseExecution(int seconds) {
        if (seconds > 0) {

            for (def start = LocalTime.now(); start.plusSeconds(seconds).isAfter(LocalTime.now()); Thread.sleep(1000)) {
                println("Execution continues in ${Seconds.secondsBetween(LocalTime.now(), start.plusSeconds(seconds)).getSeconds()} second(s)")
            }
        }
    }

    void waitUntilMaxTimeOrTargetState(int seconds, String newServiceInstanceId = serviceInstanceId) {
        int sleepTime = 1000
        if (seconds > 0) {
            for (
                    def start = LocalTime.now(); start.plusSeconds(seconds).isAfter(LocalTime.now()); Thread.sleep(sleepTime)) {
                def timeUntilForcedExecution = Seconds.secondsBetween(LocalTime.now(), start.plusSeconds(seconds)).getSeconds()
                if (timeUntilForcedExecution % 23 == 0) {
                    LastOperationState operationState = createServiceBrokerClient().getServiceInstanceLastOperation(newServiceInstanceId).getBody().state
                    if (operationState == LastOperationState.SUCCEEDED || operationState == LastOperationState.FAILED) {
                        return
                    }
                }
                println("Execution continues in ${timeUntilForcedExecution} second(s)")
            }
        }
    }


    Map<String, Object> getCredentials() {
        return credentials
    }

    def requestUpdateServiceInstance(
            final String serviceInstanceId,
            final String serviceGuid,
            final String planGuid,
            Map<String, Object> parameters = null,
            final Boolean async = false) {

        createServiceBrokerClientWithCustomErrorHandler()
                .updateServiceInstance(new UpdateServiceInstanceRequest(serviceGuid, planGuid, parameters)
                .withAsyncAccepted(async)
                .withServiceInstanceId(serviceInstanceId))
    }

    private static class NoOpResponseErrorHandler extends DefaultResponseErrorHandler {

        @Override
        void handleError(ClientHttpResponse response) throws IOException {
        }

    }

    /**
     * Find first occurrence of a UserConfig with a requested role across all guids.
     * @param role
     * @return
     */
    protected UserConfig getUserByRole(String role) {
        return userConfig.platformUsers.find { it.role == role }
    }

    void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId
        this.serviceInstanceIds << serviceInstanceId
    }

    void setServiceBindingId(String serviceBindingId) {
        this.serviceBindingId = serviceBindingId
    }

    void setPlan(Plan plan) {
        this.plan = plan
        this.plans << plan
    }
}
