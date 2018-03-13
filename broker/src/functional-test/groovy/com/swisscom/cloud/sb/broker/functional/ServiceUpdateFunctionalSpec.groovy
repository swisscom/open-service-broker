package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.repository.PlanRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceDetailRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.util.Resource
import com.swisscom.cloud.sb.broker.util.test.DummyServiceProvider
import com.swisscom.cloud.sb.client.model.LastOperationState
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.CloudFoundryContext
import org.springframework.http.HttpStatus
import spock.lang.Shared

class ServiceUpdateFunctionalSpec extends BaseFunctionalSpec {
    @Autowired
    ServiceInstanceRepository serviceInstanceRepository
    @Autowired
    PlanRepository planRepository
    @Autowired
    ServiceDetailRepository serviceDetailRepository

    @Shared
    boolean serviceDefinitionsSetUp = false

    static String notUpdatableServiceGuid = "updateTest_notUpdateable"
    static String notUpdatablePlanGuid = "updateTest_notUpdateable_plan_a"
    static String defaultServiceGuid = "updateTest_Updateable"
    static String defaultPlanGuid = "updateTest_Updateable_plan_a"
    static String defaultOrgGuid = "org_id"
    static String defaultSpaceGuid = "space_id"

    static String parameterKey = "mode"
    static String parameterOldValue = "blocking"
    static String parameterNewValue = "open"
    Map<String, Object> parameters;

    private String requestServiceProvisioning(
            Map<String, Object> parameters = new HashMap<String, Object>(),
            String planGuid = defaultPlanGuid,
            boolean async = false,
            String serviceGuid = defaultServiceGuid) {

        def serviceInstanceGuid = UUID.randomUUID().toString()
        def context = new CloudFoundryContext(defaultOrgGuid, defaultSpaceGuid)

        serviceLifeCycler.requestServiceProvisioning(
                serviceInstanceGuid,
                serviceGuid,
                planGuid,
                async,
                context,
                parameters)

        return serviceInstanceGuid
    }

    def setup() {
        if (!serviceDefinitionsSetUp) {
            serviceBrokerClient.createOrUpdateServiceDefinition(
                    Resource.readTestFileContent("/service-data/serviceDefinition_updateTest_updateable.json"))
            serviceBrokerClient.createOrUpdateServiceDefinition(
                    Resource.readTestFileContent("/service-data/serviceDefinition_updateTest_notUpdateable.json"))
            serviceDefinitionsSetUp = true
        }

        parameters = new HashMap<String, Object>()
        parameters.put(parameterKey, parameterOldValue)
    }

    def "plan can be updated"() {
        setup:
        def newPlanGuid = "updateTest_Updateable_plan_b"
        def serviceInstanceGuid = requestServiceProvisioning()

        when:
        serviceLifeCycler.requestUpdateServiceInstance(serviceInstanceGuid, defaultServiceGuid, newPlanGuid)

        then:
        def serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        assert serviceInstance
        serviceInstance.plan.guid == newPlanGuid

        cleanup:
        serviceLifeCycler.deleteServiceInstanceAndAssert(serviceInstanceGuid, defaultServiceGuid, newPlanGuid, null, false, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4)
    }

    def "parameters can be updated"() {
        given:
        def serviceInstanceGuid = requestServiceProvisioning(parameters)

        when:
        parameters.put(parameterKey, parameterNewValue)
        serviceLifeCycler.requestUpdateServiceInstance(serviceInstanceGuid, defaultServiceGuid, null, parameters)

        then:
        def serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        assert serviceInstance
        def modeDetail = serviceInstance.details.find { d -> d.key == parameterKey }
        assert modeDetail
        modeDetail.value == parameterNewValue

        cleanup:
        serviceLifeCycler.deleteServiceInstanceAndAssert(serviceInstanceGuid, defaultServiceGuid, defaultPlanGuid, null, false, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4)
    }

    def "parameters can be updated with same planId"() {
        given:
        def serviceInstanceGuid = requestServiceProvisioning(parameters)

        when:
        parameters.put(parameterKey, parameterNewValue)
        serviceLifeCycler.requestUpdateServiceInstance(serviceInstanceGuid, defaultServiceGuid, defaultPlanGuid, parameters)

        then:
        def serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        assert serviceInstance
        def modeDetail = serviceInstance.details.find { d -> d.key == parameterKey }
        assert modeDetail
        modeDetail.value == parameterNewValue

        cleanup:
        serviceLifeCycler.deleteServiceInstanceAndAssert(serviceInstanceGuid, defaultServiceGuid, defaultPlanGuid, null, false, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4)
    }

    def "parameters update changes parameters on serviceDefinition"() {
        setup:
        def serviceInstanceGuid = requestServiceProvisioning(parameters)

        when:
        parameters.put(parameterKey, parameterNewValue)
        serviceLifeCycler.requestUpdateServiceInstance(serviceInstanceGuid, defaultServiceGuid, null, parameters)

        then:
        def serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        assert serviceInstance
        serviceInstance.parameters == '{"mode":"open"}'

        cleanup:
        serviceLifeCycler.deleteServiceInstanceAndAssert(serviceInstanceGuid, defaultServiceGuid, defaultPlanGuid, null, false, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4)
    }
    
    def "plan and parameters can be updated"() {
        setup:
        def parameters = new HashMap<String, Object>()
        def parameterKey = "mode"
        def oldValue = "blocking"
        def newValue = "open"
        parameters.put(parameterKey, oldValue)
        def serviceInstanceGuid = requestServiceProvisioning(parameters)
        def newPlanGuid = "updateTest_Updateable_plan_b"

        when:
        parameters.put(parameterKey, newValue)
        serviceLifeCycler.requestUpdateServiceInstance(serviceInstanceGuid, defaultSpaceGuid, newPlanGuid, parameters)

        then:
        def serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        assert serviceInstance
        serviceInstance.plan.guid == newPlanGuid
        def modeDetail = serviceInstance.details.findAll { d -> d.key == parameterKey }
        modeDetail[0].value == newValue

        cleanup:
        serviceLifeCycler.deleteServiceInstanceAndAssert(serviceInstanceGuid, defaultSpaceGuid, newPlanGuid, null, false, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4)
    }

    def "plan update blocks if plan_updatable is false"() {
        given:
        def newPlanGuid = "updateTest_notUpdateable_plan_b"
        def serviceInstanceGuid = requestServiceProvisioning(null, notUpdatablePlanGuid, false, notUpdatableServiceGuid)

        when:
        def response = serviceLifeCycler.requestUpdateServiceInstance(serviceInstanceGuid, notUpdatableServiceGuid, newPlanGuid)

        then:
        response.statusCode == ErrorCode.PLAN_UPDATE_NOT_ALLOWED.httpStatus

        cleanup:
        serviceLifeCycler.deleteServiceInstanceAndAssert(serviceInstanceGuid, notUpdatableServiceGuid, notUpdatablePlanGuid, null, false, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4)

    }

    def "async parameter update is supported"() {
        setup:
            def serviceInstanceGuid = requestServiceProvisioning(parameters)

        when:
            parameters.put(parameterKey, parameterNewValue)
            serviceLifeCycler.requestUpdateServiceInstance(serviceInstanceGuid, defaultServiceGuid, null, parameters, true)
            waitUntilMaxTimeOrTargetState(serviceInstanceGuid, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 6)

        then:
            def lastOperationResponse = serviceBrokerClient.getServiceInstanceLastOperation(serviceInstanceGuid).getBody()
            def operationState = lastOperationResponse.state
            operationState == LastOperationState.SUCCEEDED || operationState == LastOperationState.FAILED
            def serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
            assert serviceInstance
            def modeDetail = serviceInstance.details.find { d -> d.key == parameterKey }
            assert modeDetail
            modeDetail.value == parameterNewValue

        cleanup:
            serviceLifeCycler.deleteServiceInstanceAndAssert(serviceInstanceGuid, defaultServiceGuid, defaultPlanGuid, null, false, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4)
    }

    def "async update is failing if provision is still in progress"() {
        setup:
            def newPlanGuid = "updateTest_notUpdateable_plan_b"
            def serviceInstanceGuid = requestServiceProvisioning(parameters, defaultPlanGuid, true)

        when:
            parameters.put(parameterKey, parameterNewValue)
            def response = serviceLifeCycler.requestUpdateServiceInstance(serviceInstanceGuid, defaultServiceGuid, newPlanGuid, parameters, true)

        then:
            response.statusCode == ErrorCode.OPERATION_IN_PROGRESS.httpStatus

        cleanup:
            serviceLifeCycler.deleteServiceInstanceAndAssert(serviceInstanceGuid, defaultServiceGuid, defaultPlanGuid, null, false, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4)
    }

    def "second async parameter update is denied"() {
        setup:

            def secondNewValue = "unidirectional"
            def serviceInstanceGuid = requestServiceProvisioning(parameters)

        when:
            parameters.put(parameterKey, parameterNewValue)
            def response = serviceLifeCycler.requestUpdateServiceInstance(serviceInstanceGuid, defaultServiceGuid, null, parameters, true)
            parameters.put(parameterKey, secondNewValue)
            def response2 = serviceLifeCycler.requestUpdateServiceInstance(serviceInstanceGuid, defaultSpaceGuid, null, parameters, true)
            waitUntilMaxTimeOrTargetState(serviceInstanceGuid, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 6)

        then:
            response.statusCode == HttpStatus.ACCEPTED
            response2.statusCode == ErrorCode.OPERATION_IN_PROGRESS.httpStatus
            def lastOperationResponse = serviceBrokerClient.getServiceInstanceLastOperation(serviceInstanceGuid).getBody()
            def operationState = lastOperationResponse.state
            operationState == LastOperationState.SUCCEEDED || operationState == LastOperationState.FAILED
            def serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
            assert serviceInstance
            def modeDetail = serviceInstance.details.find { d -> d.key == parameterKey }
            assert modeDetail
            modeDetail.value == parameterNewValue

        cleanup:
            serviceLifeCycler.deleteServiceInstanceAndAssert(serviceInstanceGuid, defaultServiceGuid, defaultPlanGuid, null, false, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4)
    }
}
