package com.swisscom.cloud.sb.broker.functional


import com.swisscom.cloud.sb.broker.model.repository.PlanRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceDetailRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.util.Resource
import com.swisscom.cloud.sb.broker.util.test.DummyServiceProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.CloudFoundryContext
import org.springframework.cloud.servicebroker.model.Context
import spock.lang.Shared

class CustomContext extends Context {

    CustomContext(Map<String, Object> properties) {
        super("CUSTOM", properties)
    }
}

class ServiceContextFunctionalSpec extends BaseFunctionalSpec {
    @Autowired
    ServiceInstanceRepository serviceInstanceRepository
    @Autowired
    PlanRepository planRepository
    @Autowired
    ServiceDetailRepository serviceDetailRepository

    @Shared
    boolean serviceDefinitionsSetUp = false

    static String defaultServiceGuid = "updateTest_Updateable"
    static String defaultPlanGuid = "updateTest_Updateable_plan_a"

    static String defaultOrgGuid = "org_id"
    static String defaultSpaceGuid = "space_id"

    static String parameterKey = "mode"
    static String parameterOldValue = "blocking"
    Map<String, Object> parameters

    private String requestServiceProvisioning(
            Map<String, Object> parameters = new HashMap<String, Object>(),
            String planGuid = defaultPlanGuid,
            boolean async = false,
            String serviceGuid = defaultServiceGuid,
            context = new CloudFoundryContext(defaultOrgGuid, defaultSpaceGuid)) {

        def serviceInstanceGuid = UUID.randomUUID().toString()

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

    def "can be update serviceContext"() {
        given:
        def context = new CustomContext([
                "field_a": "Some Value",
                "field_b": null,
                "field_c": 13.37,
                "field_d": 1337
        ])

        when:
        def serviceInstanceGuid = requestServiceProvisioning(new HashMap<String, Object>(),
                defaultPlanGuid,
                false,
                defaultServiceGuid,
                context)

        then:
        noExceptionThrown()
        def serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        serviceInstance.serviceContext.platform == "CUSTOM"
        serviceInstance.serviceContext.details.find { d -> d.key == "field_a" }.value == "Some Value"
        serviceInstance.serviceContext.details.find { d -> d.key == "field_c" }.value == "13.37"
        serviceInstance.serviceContext.details.find { d -> d.key == "field_d" }.value == "1337"

        when:
        context = new CustomContext([
                "field_a": "Some Other Value",
                "field_b": "Some New Value",
                "field_c": 42.42
        ])

        serviceLifeCycler.requestUpdateServiceInstance(serviceInstanceGuid, defaultServiceGuid, defaultPlanGuid, null, false, context)

        then:
        def serviceInstance2 = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        assert serviceInstance2
        serviceInstance2.serviceContext.platform == "CUSTOM"
        serviceInstance2.serviceContext.details.find { d -> d.key == "field_a" }.value == "Some Other Value"
        serviceInstance2.serviceContext.details.find { d -> d.key == "field_b" }.value == "Some New Value"
        serviceInstance2.serviceContext.details.find { d -> d.key == "field_c" }.value == "42.42"
        serviceInstance2.serviceContext.details.size() == 3

        cleanup:
        serviceLifeCycler.deleteServiceInstanceAndAssert(serviceInstanceGuid, defaultServiceGuid, defaultPlanGuid, null, false, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4)
    }

    def "sending no context does not delete context"() {
        given:
        def context = new CustomContext([
                "field_a": "Some Value",
                "field_b": null,
                "field_c": 13.37,
                "field_d": 1337
        ])

        when:
        def serviceInstanceGuid = requestServiceProvisioning(new HashMap<String, Object>(),
                defaultPlanGuid,
                false,
                defaultServiceGuid,
                context)

        then:
        noExceptionThrown()
        def serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        serviceInstance.serviceContext.platform == "CUSTOM"
        serviceInstance.serviceContext.details.find { d -> d.key == "field_a" }.value == "Some Value"
        serviceInstance.serviceContext.details.find { d -> d.key == "field_c" }.value == "13.37"
        serviceInstance.serviceContext.details.find { d -> d.key == "field_d" }.value == "1337"
        serviceInstance.serviceContext.details.size() == 4

        when:
        context = null
        serviceLifeCycler.requestUpdateServiceInstance(serviceInstanceGuid, defaultServiceGuid, defaultPlanGuid, null, false, context)

        then:
        def serviceInstance2 = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        assert serviceInstance2
        serviceInstance2.serviceContext.platform == "CUSTOM"
        serviceInstance2.serviceContext.details.find { d -> d.key == "field_a" }.value == "Some Value"
        serviceInstance2.serviceContext.details.find { d -> d.key == "field_c" }.value == "13.37"
        serviceInstance2.serviceContext.details.find { d -> d.key == "field_d" }.value == "1337"
        serviceInstance2.serviceContext.details.size() == 4

        cleanup:
        serviceLifeCycler.deleteServiceInstanceAndAssert(serviceInstanceGuid, defaultServiceGuid, defaultPlanGuid, null, false, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4)
    }
}