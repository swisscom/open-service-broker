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

import com.swisscom.cloud.sb.broker.BaseTransactionalSpecification
import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.CFServicePermission
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.repository.*
import com.swisscom.cloud.sb.broker.util.DBTestUtil
import com.swisscom.cloud.sb.broker.util.Resource
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
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


    private CFService findService() {
        return cfServiceRepository.findByGuid(serviceGuid)
    }


    def assertServicePermissions() {
        def permissions = [CFServicePermission.SYSLOG_DRAIN]
        CFService service = findService()
        permissions.each {
            String p -> assert service.permissions.find { it.permission == p }
        }
        service.permissions.size() == 1
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

}
