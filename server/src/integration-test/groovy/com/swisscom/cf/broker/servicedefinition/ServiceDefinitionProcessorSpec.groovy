package com.swisscom.cf.broker.servicedefinition

import com.fasterxml.jackson.databind.ObjectMapper
import com.swisscom.cf.broker.BaseTransactionalSpecification
import com.swisscom.cf.broker.error.ErrorCode
import com.swisscom.cf.broker.error.ServiceBrokerException
import com.swisscom.cf.broker.model.*
import com.swisscom.cf.broker.model.repository.*
import com.swisscom.cf.broker.util.DBTestUtil
import com.swisscom.cf.broker.util.test.ErrorCodeHelper
import groovy.json.JsonSlurper
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared
import spock.lang.Unroll

class ServiceDefinitionProcessorSpec extends BaseTransactionalSpecification {
    public static final String FILE_SERVICE1 = "/service-data/service1.json"
    public static
    final String FILE_SERVICE1_WITHOUT_PLAN_INTERNAL_NAME = "/service-data/service1WithoutPlanInternalName.json"
    public static
    final String FILE_SERVICE1_WITHOUT_BACKUP_CAPABLE_SP = "/service-data/service1WithoutBackupCapableServiceProvider.json"

    public static final String INTERNAL_NAME = "dummySynchronousBackupCapable"
    public static
    final String FILE_SERVICE1_WITHOUT_DASHBOARD_CLIENT = "/service-data/service1WithoutDashboardClient.json"

    @Shared
    private String serviceGuid
    @Shared
    private String planGuid
    @Autowired
    private ServiceDefinitionProcessor serviceDefinitionProcessor

    @Autowired
    private CFServiceRepository cfServiceRepository
    @Autowired
    private PlanRepository planRepository
    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository
    @Autowired
    private TagRepository tagRepository
    @Autowired
    private PlanMetadataRepository planMetadataRepository
    @Autowired
    private CFServiceMetaDataRepository cfServiceMetaDataRepository
    @Autowired
    private CFServicePermissionRepository servicePermissionRepository
    @Autowired
    private ParameterRepository parameterRepository

    @Autowired
    private DBTestUtil dbTestUtil

    def setupSpec() {
        def json = new JsonSlurper().parseText(readTestFileContent(FILE_SERVICE1))
        serviceGuid = json.guid
        planGuid = json.plans[0].guid
    }


    def "new service basic details are added correctly"() {
        given:
        assureServiceDoesNotExist()
        when:
        processServiceDefinition()
        then:
        assertServiceBasicDetails()
    }

    def "existing service basic details are updated correctly"() {
        given:
        createService()
        when:
        processServiceDefinition()
        then:
        assertServiceBasicDetails()
    }

    def "services which don't include the asyncRequired json element are processed correctly"() {
        given:
        CFService service = createService()
        when:
        processServiceDefinition(FILE_SERVICE1_WITHOUT_PLAN_INTERNAL_NAME)
        then:
        assertServiceBasicDetails(false, false)
        findService().asyncRequired == null
    }


    def "service basic details that does not contain dashboard client is handled correctly"() {
        given:
        createService()
        when:
        processServiceDefinition(FILE_SERVICE1_WITHOUT_DASHBOARD_CLIENT)
        then:
        assertServiceBasicDetails(false, false)
    }

    private def createService() {
        assureServiceDoesNotExist()
        def service = new CFService(guid: serviceGuid)
        return cfServiceRepository.save(service)
    }

    private def assureServiceDoesNotExist() {
        assert !cfServiceRepository.findByGuid(serviceGuid)
    }

    private def assertServiceBasicDetails(boolean assertAsync = true, boolean assertDashboardDetails = true) {
        CFService service = findService()
        assert service.name == 'service1Name'
        assert service.description == 'description'
        assert service.bindable
        assert service.internalName == INTERNAL_NAME
        assert service.displayIndex == 1
        if (assertAsync) {
            assert service.asyncRequired == true
        }
        if (assertDashboardDetails) {
            assert service.dashboardClientId == "dashboardId"
            assert service.dashboardClientSecret == 'secret'
            assert service.dashboardClientRedirectUri == "https://dashboard.service.com"
        }
        return service
    }

    private CFService findService() {
        return cfServiceRepository.findByGuid(serviceGuid)
    }

    def "new service tags are added correctly"() {
        given:
        createService()
        when:
        processServiceDefinition()
        then:
        assertServiceTags()
    }

    def "old service tags are removed correctly"() {
        given:
        CFService service = createService()
        ['tagOld1', 'tagOld2'].each {
            Tag t = new Tag(tag: it)
            tagRepository.save(t)
            service.tags.add(t)
            cfServiceRepository.save(service)
        }
        when:
        processServiceDefinition()
        then:
        assertServiceTags()
    }

    private def assertServiceTags() {
        CFService service = findService()
        service.tags.size() == 2
        ['tag1', 'tag2'].each { String s -> assert service.tags.find { it.tag == s } }
    }

    def "new service permissions are added correctly"() {
        given:
        createService()
        when:
        processServiceDefinition()
        then:
        assertServicePermissions()
    }

    def "old service permissions are removed correctly"() {
        given:
        CFService service = createService()
        ['oldPermission1', 'oldPermission2'].each {
            CFServicePermission permission = servicePermissionRepository.save(new CFServicePermission(permission: it))
            findService().permissions.add(permission)
            cfServiceRepository.save(service)
        }
        when:
        processServiceDefinition()
        then:
        assertServicePermissions()
    }

    def assertServicePermissions() {
        def permissions = [CFServicePermission.SYSLOG_DRAIN]
        CFService service = findService()
        permissions.each {
            String p -> assert service.permissions.find { it.permission == p }
        }
        service.permissions.size() == 1
    }

    def "new service metadata is added correctly"() {
        given:
        createService()
        when:
        processServiceDefinition()
        then:
        assertMetadata()
    }

    def "old service metadata is removed"() {
        given:
        CFService service = createService()
        ['key1': 'valueOld', 'key2': 'false'].each {
            CFServiceMetadata metadata = cfServiceMetaDataRepository.save(new CFServiceMetadata(key: it))
            findService().metadata.add(metadata)
            cfServiceRepository.save(service)
        }
        when:
        processServiceDefinition()
        then:
        assertMetadata()
    }

    private def assertMetadata() {
        def metadata = ['key1': 'value1', 'key2': 'true']
        CFService service = findService()
        metadata.each {
            k, v -> assert service.metadata.find { it.key == k }.value == v
        }
        service.metadata.size() == 2
    }

    def "old service plans which are **not** in use are removed"() {
        given:
        CFService service = createService()
        def plan = createPlan(service, 'newPlanGuid1234')
        when:
        processServiceDefinition()
        then:
        !planRepository.findByGuid(plan.guid)
    }

    @Unroll("finding existing plans functions correctly")
    def "finding existing plans functions correctly"() {
        given:
        def plan = planRepository.save(new Plan(guid: guid))
        expect:
        result == serviceDefinitionProcessor.isPlanIncludedInJson(new JsonSlurper().parseText("""{
                                                                                                    "plans": [{"guid": "${
            planGuid
        }"}]
                                                                                                } """), plan)
        where:
        guid            | result
        planGuid        | true
        "someOtherGuid" | false
    }

    private Plan createPlan(CFService service, String planId) {
        def plan = planRepository.save(new Plan(guid: planId))
        service.plans.add(plan)
        cfServiceRepository.save(service)
        return plan
    }

    def "old service plans which are in use are **not** removed and exception is thrown"() {
        given:
        CFService service = createService()
        def plan = createPlan(service, 'newPlanGuid1234')
        and:
        def serviceInstance = dbTestUtil.createServiceInstace(service, 'serviceInstanceGuid')
        when:
        processServiceDefinition()
        then:
        def ex = thrown(ServiceBrokerException)
        ErrorCodeHelper.assertServiceBrokerException(ex, ErrorCode.PLAN_IN_USE)
        planRepository.findByGuid(plan.guid)
        cleanup:
        serviceInstanceRepository.delete(serviceInstance)
    }

    def "old service plans which are still in use should **not** be removed"() {
        given:
        CFService service = createService()
        def plan = createPlan(service, planGuid)
        when:
        processServiceDefinition()
        then:
        plan.id == planRepository.findByGuid(plan.guid).id
        noExceptionThrown()
    }

    def "new plan basic details are added correctly"() {
        given:
        CFService service = createService()
        when:
        processServiceDefinition()
        then:
        assertPlanBasicDetails()
    }

    def "old plan basic details are updated correctly"() {
        given:
        CFService service = createService()
        and:
        Plan plan = planRepository.save(new Plan(guid: planGuid, name: 'oldName'))
        service.plans.add(plan)
        cfServiceRepository.save(service)
        when:
        processServiceDefinition()
        then:
        assertPlanBasicDetails()
        planRepository.findByGuid(planGuid).internalName == INTERNAL_NAME
        planRepository.findByGuid(planGuid).asyncRequired == true
    }

    def "plans which don't include the internalName json element are processed correctly"() {
        given:
        CFService service = createService()
        when:
        processServiceDefinition(FILE_SERVICE1_WITHOUT_PLAN_INTERNAL_NAME)
        then:
        assertPlanBasicDetails(false, false)
        planRepository.findByGuid(planGuid).internalName == null
    }

    def "plans which don't include the asyncRequired json element are processed correctly"() {
        given:
        CFService service = createService()
        when:
        processServiceDefinition(FILE_SERVICE1_WITHOUT_PLAN_INTERNAL_NAME)
        then:
        assertPlanBasicDetails(false, false)
        planRepository.findByGuid(planGuid).asyncRequired == null
    }

    def assertPlanBasicDetails(boolean checkAsync = true, boolean checkMaxBackups = true) {
        Plan plan = planRepository.findByGuid(planGuid)
        assert plan.name == "small"
        assert plan.description == "planDescription"
        assert plan.templateUniqueIdentifier == "templateV1"
        assert !plan.free
        assert plan.displayIndex == 1
        if (checkAsync) {
            assert plan.asyncRequired == true
        }
        if (checkMaxBackups) {
            assert plan.maxBackups == 5
        }
        return plan
    }

    def "new plan metadata is added correctly"() {
        given:
        CFService service = createService()
        when:
        processServiceDefinition()
        then:
        assertPlanMetadata()
    }

    private def assertPlanMetadata() {
        Plan plan = planRepository.findByGuid(planGuid)
        def metadata = ['key1': 'value1', 'key2': true]

        metadata.each {
            k, v ->
                PlanMetadata planMetadata = plan.metadata.find { it.key == k }
                assert planMetadata.type == v.class.getSimpleName()
                assert planMetadata.value == v.toString()
        }
        plan.metadata.size() == 2
        return plan
    }

    def "old plan metadata is removed correctly"() {
        given:
        CFService service = createService()
        def plan = createPlan(service, planGuid)
        [0..3].each {
            PlanMetadata planMetadata = planMetadataRepository.save(new PlanMetadata(key: 'key' + it, value: 'value' + it, plan: plan))
            plan.metadata.add(planMetadata)
            planRepository.save(plan)
        }
        when:
        processServiceDefinition()
        then:
        assertPlanMetadata()
    }

    def "new plan parameters are added correctly"() {
        when:
        processServiceDefinition()
        then:
        assertPlanParameters()
    }

    def "old plan parameters are removed correctly"() {
        given:
        CFService service = createService()
        def plan = createPlan(service, planGuid)
        [0..3].each {
            Parameter parameter = parameterRepository.save(new Parameter())
            plan.parameters.add(parameter)
            planRepository.save(plan)
        }
        when:
        processServiceDefinition()
        then:
        assertPlanMetadata()
    }


    private def assertPlanParameters() {
        Plan plan = planRepository.findByGuid(planGuid)
        def expectedParameters = [new Parameter(template: 'template1', name: 'memory', value: '2g'),
                                  new Parameter(template: 'template2', name: 'cpu', value: '1')]
        assert plan.parameters.size() == 2
        expectedParameters.each {
            Parameter expected ->
                assert plan.parameters.find {
                    Parameter p -> p.template == expected.template && p.name == expected.name && p.value == expected.value
                }
        }
        return plan
    }

    private def processServiceDefinition(String fileName = FILE_SERVICE1) {
        return serviceDefinitionProcessor.createOrUpdateServiceDefinition(readTestFileContent(fileName))
    }

    def "service which does not have instances is deleted correctly"() {
        given:
        processServiceDefinition()
        when:
        serviceDefinitionProcessor.deleteServiceDefinition(serviceGuid)
        then:
        !cfServiceRepository.findByGuid(serviceGuid)
    }

    def "attempting to delete a service that does not exist throws the right exception"() {
        given:
        processServiceDefinition()
        when:
        serviceDefinitionProcessor.deleteServiceDefinition('thereSHouldBeNoSuchService')
        then:
        def ex = thrown(ServiceBrokerException)
        ErrorCodeHelper.assertServiceBrokerException(ex, ErrorCode.SERVICE_NOT_FOUND)
    }

    def "service which has instances should not be allowed to be deleted"() {
        given:
        processServiceDefinition()
        CFService service = cfServiceRepository.findByGuid(serviceGuid)
        ServiceInstance serviceInstance = dbTestUtil.createServiceInstace(service, 'serviceInstance1234')
        when:
        serviceDefinitionProcessor.deleteServiceDefinition(serviceGuid)
        then:
        def ex = thrown(ServiceBrokerException)
        ErrorCodeHelper.assertServiceBrokerException(ex, ErrorCode.SERVICE_IN_USE)
        cleanup:
        serviceInstanceRepository.delete(serviceInstance)
    }

    def "service plans that have >0 maxBackups should cause an exception when configured service provider is not backup capable"() {
        when:
        processServiceDefinition(FILE_SERVICE1_WITHOUT_BACKUP_CAPABLE_SP)
        then:
        thrown(RuntimeException)
    }

    def "service definition is produced correctly"() {
        given:
        processServiceDefinition(FILE_SERVICE1)
        when:
        def dto = serviceDefinitionProcessor.getServiceDefinition(serviceGuid)
        then:
        JSONAssert.assertEquals(readTestFileContent(FILE_SERVICE1), new ObjectMapper().writeValueAsString(dto), JSONCompareMode.LENIENT)
    }
}
