/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.broker.servicedefinition

import com.fasterxml.jackson.databind.ObjectMapper
import com.swisscom.cloud.sb.broker.BaseTransactionalSpecification
import com.swisscom.cloud.sb.broker.cfapi.converter.MetadataJsonHelper
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.CFServiceMetadata
import com.swisscom.cloud.sb.broker.model.CFServicePermission
import com.swisscom.cloud.sb.broker.model.Parameter
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.PlanMetadata
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.Tag
import com.swisscom.cloud.sb.broker.repository.CFServiceMetaDataRepository
import com.swisscom.cloud.sb.broker.repository.CFServicePermissionRepository
import com.swisscom.cloud.sb.broker.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.repository.ParameterRepository
import com.swisscom.cloud.sb.broker.repository.PlanMetadataRepository
import com.swisscom.cloud.sb.broker.repository.PlanRepository
import com.swisscom.cloud.sb.broker.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.repository.TagRepository
import com.swisscom.cloud.sb.broker.servicedefinition.dto.ServiceDto
import com.swisscom.cloud.sb.broker.util.DBTestUtil
import com.swisscom.cloud.sb.broker.util.Resource
import com.swisscom.cloud.sb.broker.util.test.ErrorCodeHelper
import groovy.json.JsonSlurper
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import spock.lang.Shared
import spock.lang.Unroll

class ServiceDefinitionProcessorSpec extends BaseTransactionalSpecification {
    public static final String FILE_SERVICE1 = "/service-data/service1.json"
    public static
    final String FILE_SERVICE1_WITHOUT_PLAN_INTERNAL_NAME = "/service-data/service1WithoutPlanInternalName.json"
    public static
    final String FILE_SERVICE1_WITHOUT_BACKUP_CAPABLE_SP = "/service-data/service1WithoutBackupCapableServiceProvider.json"
    public static
    final String FILE_SERVICE1_WITH_INVALID_JSON = "/service-data/service1WithInvalidJson.json"

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
        def json = new JsonSlurper().parseText(Resource.readTestFileContent(FILE_SERVICE1))
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
        def metadata = ['key1': "value1", 'key2': "true"]
        CFService service = findService()
        metadata.each {
            k, v -> assert service.metadata.find { it.key == k }.value == v
        }
        service.metadata.size() == 3
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
        def plan = planRepository.findByGuid(planId)
        if (plan == null) {
            plan = planRepository.save(new Plan(guid: planId))
        }
        if (!service.plans.any { p -> p.guid == planId }) {
            service.plans.add(plan)
            cfServiceRepository.save(service)
        }
        
        return plan
    }

    def "old service plans which are in use are **not** removed but set inactive"() {
        given:
        CFService service = createService()
        def plan = createPlan(service, 'newPlanGuid1234')
        and:
        def serviceInstance = dbTestUtil.createServiceInstace(service, 'serviceInstanceGuid')
        when:
        processServiceDefinition()
        then:
        planRepository.findByGuid(plan.guid)
        plan.active == false

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
                assert planMetadata.type == v.class.name
                assert v == MetadataJsonHelper.getValue(planMetadata.type, planMetadata.value)
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
        return serviceDefinitionProcessor.createOrUpdateServiceDefinition(Resource.readTestFileContent(fileName))
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
        JSONAssert.assertEquals(Resource.readTestFileContent(FILE_SERVICE1), new ObjectMapper().writeValueAsString(dto), JSONCompareMode.LENIENT)
    }

    def "service definition with invalid json should be rejected"() {
        when:
        processServiceDefinition(FILE_SERVICE1_WITH_INVALID_JSON)
        then:
        def ex = thrown(ServiceBrokerException)
        ex.httpStatus == HttpStatus.BAD_REQUEST
        ex.code == ErrorCode.INVALID_JSON.code
    }

    def "service with correct specified json schema"() {
        given:
        def serviceInstanceCreateSchema = '{"$schema":"http://json-schema.org/draft-04/schema#","type":"object","properties":{"billing-account":{"description":"Service instance create","type":"string"}}}'
        def serviceInstanceUpdateSchema = '{"$schema":"http://json-schema.org/draft-04/schema#","type":"object","properties":{"billing-account":{"description":"Service instance update","type":"string"}}}'
        def serviceBindingCreateSchema = '{"$schema":"http://json-schema.org/draft-04/schema#","type":"object","properties":{"billing-account":{"description":"Service binding create","type":"string"}}}'
        def service = createService() as CFService

        when:
        processServiceDefinition('/service-data/service1_plan_valid_json_schema.json')

        then:
        assertServiceBasicDetails(false, false)
        def plan = service.plans[0]
        plan.serviceInstanceCreateSchema == serviceInstanceCreateSchema
        plan.serviceInstanceUpdateSchema == serviceInstanceUpdateSchema
        plan.serviceBindingCreateSchema == serviceBindingCreateSchema
    }

    def "service plan with invalid json schema should be rejected"() {
        given:
        createService() as CFService

        when:
        processServiceDefinition('/service-data/service1_plan_invalid_json_schema.json')

        then:
        def ex = thrown(ServiceBrokerException)
        ex.httpStatus == HttpStatus.BAD_REQUEST
        ex.code == ErrorCode.INVALID_PLAN_SCHEMAS.code
    }

}
