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

    def setup() {
        if (!serviceDefinitionsSetUp) {
            serviceBrokerClient.createOrUpdateServiceDefinition(
                    Resource.readTestFileContent("/service-data/serviceDefinition_updateTest_updateable.json"))
            serviceBrokerClient.createOrUpdateServiceDefinition(
                    Resource.readTestFileContent("/service-data/serviceDefinition_updateTest_notUpdateable.json"))

            serviceDefinitionsSetUp = true
        }
    }

    def "plan can be updated"() {
        given:
        def parameters = new HashMap<String, Object>()
        def context = new CloudFoundryContext("org_id", "space_id")
        def serviceInstanceGuid = UUID.randomUUID().toString()
        def serviceGuid = "updateTest_Updateable"
        def planGuid = "updateTest_Updateable_plan_a"
        def newPlanGuid = "updateTest_Updateable_plan_b"
        serviceLifeCycler.requestServiceProvisioning(
                serviceInstanceGuid,
                serviceGuid,
                planGuid,
                false,
                context,
                parameters
        )

        when:
        serviceLifeCycler.requestUpdateServiceInstance(serviceInstanceGuid, serviceGuid, newPlanGuid)

        then:
        def serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        assert serviceInstance
        serviceInstance.plan.guid == newPlanGuid

        cleanup:
        serviceLifeCycler.deleteServiceInstanceAndAssert(serviceInstanceGuid, serviceGuid, planGuid, null, false, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4)
    }

    def "parameters can be updated"() {
        given:
        def parameters = new HashMap<String, Object>()

        def parameterKey = "mode"
        def oldValue = "blocking"
        def newValue = "open"

        parameters.put(parameterKey, oldValue)

        def context = new CloudFoundryContext("org_id", "space_id")
        def serviceInstanceGuid = UUID.randomUUID().toString()
        def serviceGuid = "updateTest_Updateable"
        def planGuid = "updateTest_Updateable_plan_a"
        def newPlanGuid = "updateTest_Updateable_plan_a"
        serviceLifeCycler.requestServiceProvisioning(
                serviceInstanceGuid,
                serviceGuid,
                planGuid,
                false,
                context,
                parameters
        )

        when:
        parameters.put(parameterKey, newValue)
        serviceLifeCycler.requestUpdateServiceInstance(serviceInstanceGuid, serviceGuid, newPlanGuid, parameters)

        then:
        def serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        assert serviceInstance
        def modeDetail = serviceInstance.details.find { d -> d.key == parameterKey }
        assert modeDetail
        modeDetail.value == newValue

        cleanup:
        serviceLifeCycler.deleteServiceInstanceAndAssert(serviceInstanceGuid, serviceGuid, planGuid, null, false, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4)
    }

    def "plan and parameters can be updated"() {
        given:
        def parameters = new HashMap<String, Object>()

        def parameterKey = "mode"
        def oldValue = "blocking"
        def newValue = "open"

        parameters.put(parameterKey, oldValue)

        def context = new CloudFoundryContext("org_id", "space_id")
        def serviceInstanceGuid = UUID.randomUUID().toString()
        def serviceGuid = "updateTest_Updateable"
        def planGuid = "updateTest_Updateable_plan_a"
        def newPlanGuid = "updateTest_Updateable_plan_b"
        serviceLifeCycler.requestServiceProvisioning(
                serviceInstanceGuid,
                serviceGuid,
                planGuid,
                false,
                context,
                parameters
        )

        when:
        parameters.put(parameterKey, newValue)
        serviceLifeCycler.requestUpdateServiceInstance(serviceInstanceGuid, serviceGuid, newPlanGuid, parameters)

        then:
        def serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        assert serviceInstance
        serviceInstance.plan.guid == newPlanGuid
        def modeDetail = serviceInstance.details.findAll { d -> d.key == parameterKey }
        modeDetail[0].value == newValue

        cleanup:
        serviceLifeCycler.deleteServiceInstanceAndAssert(serviceInstanceGuid, serviceGuid, planGuid, null, false, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4)
    }

    def "plan update blocks if plan_updatable is false"() {
        given:
        def parameters = new HashMap<String, Object>()
        def context = new CloudFoundryContext("org_id", "space_id")
        def serviceInstanceGuid = UUID.randomUUID().toString()
        def serviceGuid = "updateTest_notUpdateable"
        def planGuid = "updateTest_notUpdateable_plan_a"
        def newPlanGuid = "updateTest_notUpdateable_plan_b"
        serviceLifeCycler.requestServiceProvisioning(
                serviceInstanceGuid,
                serviceGuid,
                planGuid,
                false,
                context,
                parameters
        )

        when:
        def response = serviceLifeCycler.requestUpdateServiceInstance(serviceInstanceGuid, serviceGuid, newPlanGuid)

        then:
        response.statusCode == ErrorCode.PLAN_UPDATE_NOT_ALLOWED.httpStatus

        cleanup:
        serviceLifeCycler.deleteServiceInstanceAndAssert(serviceInstanceGuid, serviceGuid, planGuid, null, false, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4)

    }

    def "async parameter update is supported"() {
        setup:
            def parameters = new HashMap<String, Object>()
            def parameterKey = "mode"
            def oldValue = "blocking"
            def newValue = "open"
            parameters.put(parameterKey, oldValue)
            def context = new CloudFoundryContext("org_id", "space_id")
            def serviceInstanceGuid = UUID.randomUUID().toString()
            def serviceGuid = "updateTest_Updateable"
            def planGuid = "updateTest_Updateable_plan_a"
            def newPlanGuid = "updateTest_Updateable_plan_a"
            serviceLifeCycler.requestServiceProvisioning(
                    serviceInstanceGuid,
                    serviceGuid,
                    planGuid,
                    false,
                    context,
                    parameters
            )

        when:
            parameters.put(parameterKey, newValue)
            serviceLifeCycler.requestUpdateServiceInstance(serviceInstanceGuid, serviceGuid, newPlanGuid, parameters, true)
            waitUntilMaxTimeOrTargetState(serviceInstanceGuid, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 6)

        then:
            def lastOperationResponse = serviceBrokerClient.getServiceInstanceLastOperation(serviceInstanceGuid).getBody();
            def operationState = lastOperationResponse.state;
            operationState == LastOperationState.SUCCEEDED || operationState == LastOperationState.FAILED
            def serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
            assert serviceInstance
            def modeDetail = serviceInstance.details.find { d -> d.key == parameterKey }
            assert modeDetail
            modeDetail.value == newValue

        cleanup:
            serviceLifeCycler.deleteServiceInstanceAndAssert(serviceInstanceGuid, serviceGuid, planGuid, null, false, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4)
    }

    def "async update is failing if provision is still in progress"() {
        setup:
            def parameters = new HashMap<String, Object>()

            def parameterKey = "mode"
            def oldValue = "blocking"
            def newValue = "open"

            parameters.put(parameterKey, oldValue)

            def context = new CloudFoundryContext("org_id", "space_id")
            def serviceInstanceGuid = UUID.randomUUID().toString()
            def serviceGuid = "updateTest_Updateable"
            def planGuid = "updateTest_Updateable_plan_a"
            def newPlanGuid = "updateTest_Updateable_plan_a"
            serviceLifeCycler.requestServiceProvisioning(
                    serviceInstanceGuid,
                    serviceGuid,
                    planGuid,
                    true,
                    context,
                    parameters
            )

        when:
            parameters.put(parameterKey, newValue)
            def response = serviceLifeCycler.requestUpdateServiceInstance(serviceInstanceGuid, serviceGuid, newPlanGuid, parameters, true)

        then:
            response.statusCode == ErrorCode.OPERATION_IN_PROGRESS.httpStatus

        cleanup:
            serviceLifeCycler.deleteServiceInstanceAndAssert(serviceInstanceGuid, serviceGuid, planGuid, null, false, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4)
    }

    def "second async parameter update is denied"() {
        setup:
            def parameters = new HashMap<String, Object>()
            def parameterKey = "mode"
            def oldValue = "blocking"
            def newValue = "open"
            def secondNewValue = "unidirectional"
            parameters.put(parameterKey, oldValue)
            def context = new CloudFoundryContext("org_id", "space_id")
            def serviceInstanceGuid = UUID.randomUUID().toString()
            def serviceGuid = "updateTest_Updateable"
            def planGuid = "updateTest_Updateable_plan_a"
            def newPlanGuid = "updateTest_Updateable_plan_a"
            serviceLifeCycler.requestServiceProvisioning(
                    serviceInstanceGuid,
                    serviceGuid,
                    planGuid,
                    false,
                    context,
                    parameters
            )

        when:
            parameters.put(parameterKey, newValue)
            def response = serviceLifeCycler.requestUpdateServiceInstance(serviceInstanceGuid, serviceGuid, newPlanGuid, parameters, true)
            parameters.put(parameterKey, secondNewValue)
            def response2 = serviceLifeCycler.requestUpdateServiceInstance(serviceInstanceGuid, serviceGuid, newPlanGuid, parameters, true)
            waitUntilMaxTimeOrTargetState(serviceInstanceGuid, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 6)

        then:
            response.statusCode == HttpStatus.ACCEPTED
            response2.statusCode == ErrorCode.OPERATION_IN_PROGRESS.httpStatus
            def lastOperationResponse = serviceBrokerClient.getServiceInstanceLastOperation(serviceInstanceGuid).getBody();
            def operationState = lastOperationResponse.state;
            operationState == LastOperationState.SUCCEEDED || operationState == LastOperationState.FAILED
            def serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
            assert serviceInstance
            def modeDetail = serviceInstance.details.find { d -> d.key == parameterKey }
            assert modeDetail
            modeDetail.value == newValue

        cleanup:
            serviceLifeCycler.deleteServiceInstanceAndAssert(serviceInstanceGuid, serviceGuid, planGuid, null, false, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4)
    }
}
