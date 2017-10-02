package com.swisscom.cloud.sb.broker.services.mongodb.enterprise.statemachine

import com.swisscom.cloud.sb.broker.model.*
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.MongoDbEnterpriseServiceDetailKey
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.MongoDbEnterpriseConfig
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.MongoDbEnterpriseDeployment
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.MongoDbEnterpriseFreePortFinder
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.opsmanager.OpsManagerFacade
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.opsmanager.OpsManagerGroup
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailKey
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper
import spock.lang.Specification

class MongoDbEnterpriseProvisionStateSpec extends Specification {
    private MongoDbEnterperiseStateMachineContext context


    def setup() {
        context = new MongoDbEnterperiseStateMachineContext()
        context.opsManagerFacade = Mock(OpsManagerFacade)
        context.mongoDbEnterpriseFreePortFinder = Mock(MongoDbEnterpriseFreePortFinder)
        context.mongoDbEnterpriseConfig = Stub(MongoDbEnterpriseConfig)
    }

    def "CREATE_OPS_MANAGER_GROUP"() {
        given:
        context.lastOperationJobContext = new LastOperationJobContext(provisionRequest: new ProvisionRequest(serviceInstanceGuid: 'guid'))
        and:
        def opsManagerGroup = new OpsManagerGroup(agentApiKey: 'agentApiKey', groupId: 'groupId', groupName: 'groupName')
        1 * context.opsManagerFacade.createGroup('guid') >> opsManagerGroup
        and:
        1 * context.mongoDbEnterpriseFreePortFinder.findFreePorts(1) >> [666]

        when:
        def result = MongoDbEnterpriseProvisionState.CREATE_OPS_MANAGER_GROUP.triggerAction(context)

        then:
        result.go2NextState
        def helper = ServiceDetailsHelper.from(result.details)
        helper.getValue(ServiceDetailKey.PORT) == '666'
        helper.getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_GROUP_ID) == opsManagerGroup.groupId
        helper.getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_GROUP_NAME) == opsManagerGroup.groupName
        helper.getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_AGENT_API_KEY) == opsManagerGroup.agentApiKey
        helper.getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_HEALTH_CHECK_USER)
        helper.getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_HEALTH_CHECK_PASSWORD)
    }

    def "CHECK_AGENTS"() {
        given:
        def groupId = 'GroupId'
        def agentCount = 3
        context.lastOperationJobContext = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: [ServiceDetail.from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_TARGET_AGENT_COUNT, agentCount.toString()),
                                                                                                                     ServiceDetail.from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_GROUP_ID, groupId
                                                                                                                     )]))
        and:
        1 * context.opsManagerFacade.areAgentsReady(groupId, agentCount) >> opsManagerResponse

        when:
        def result = MongoDbEnterpriseProvisionState.CHECK_AGENTS.triggerAction(context)

        then:
        result.go2NextState == go2NextState

        where:
        opsManagerResponse | go2NextState
        true               | true
        false              | false

    }

    def "REQUEST_AUTOMATION_UPDATE for a mongodb version specified in plan"() {
        given:
        def groupId = 'GroupId'
        def port = 666
        def mongodb_enterprise_health_check_user = 'MONGODB_ENTERPRISE_HEALTH_CHECK_USER'
        def mongodb_enterprise_health_check_password = 'MONGODB_ENTERPRISE_HEALTH_CHECK_PASSWORD'
        def mongodb_version = 'Some Version'
        context.lastOperationJobContext = new LastOperationJobContext(plan: new Plan(parameters: [new Parameter(name: MongoDbEnterpriseProvisionState.PLAN_PARAMETER_MONGODB_VERSION, value: mongodb_version)]),
                serviceInstance: new ServiceInstance(details: [ServiceDetail.from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_GROUP_ID, groupId),
                                                               ServiceDetail.from(ServiceDetailKey.PORT, port.toString()),
                                                               ServiceDetail.from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_HEALTH_CHECK_PASSWORD, mongodb_enterprise_health_check_password),
                                                               ServiceDetail.from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_HEALTH_CHECK_USER, mongodb_enterprise_health_check_user)])
                , provisionRequest: new ProvisionRequest(serviceInstanceGuid: 'guid'))
        and:
        def initialAutomationVersion = 1
        1 * context.opsManagerFacade.getAndCheckInitialAutomationGoalVersion(groupId) >> initialAutomationVersion

        and:
        def deployment = new MongoDbEnterpriseDeployment()
        1 * context.opsManagerFacade.deployReplicaSet(groupId, 'guid', port, mongodb_enterprise_health_check_user, mongodb_enterprise_health_check_password, mongodb_version) >> deployment

        when:
        def result = MongoDbEnterpriseProvisionState.REQUEST_AUTOMATION_UPDATE.triggerAction(context)

        then:
        result.go2NextState
        def helper = ServiceDetailsHelper.from(result.details)

        helper.getValue(ServiceDetailKey.DATABASE) == deployment.database
        helper.getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_TARGET_AUTOMATION_GOAL_VERSION) == (initialAutomationVersion + 1).toString()
        helper.getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_REPLICA_SET) == deployment.replicaSet
        helper.getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_MONITORING_AGENT_USER) == deployment.monitoringAgentUser
        helper.getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_MONITORING_AGENT_PASSWORD) == deployment.monitoringAgentPassword
        helper.getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_BACKUP_AGENT_USER) == deployment.backupAgentUser
        helper.getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_BACKUP_AGENT_PASSWORD) == deployment.backupAgentPassword
    }

    def "REQUEST_AUTOMATION_UPDATE without a mongodb version specified in plan"() {
        given:
        def groupId = 'GroupId'
        def port = 666
        def mongodb_enterprise_health_check_user = 'MONGODB_ENTERPRISE_HEALTH_CHECK_USER'
        def mongodb_enterprise_health_check_password = 'MONGODB_ENTERPRISE_HEALTH_CHECK_PASSWORD'
        def mongodb_version = 'A version from SB Config'

        and:
        context.mongoDbEnterpriseConfig = new MongoDbEnterpriseConfig(mongoDbVersion: mongodb_version)
        and:
        context.lastOperationJobContext = new LastOperationJobContext(plan: new Plan(),
                serviceInstance: new ServiceInstance(details: [ServiceDetail.from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_GROUP_ID, groupId),
                                                               ServiceDetail.from(ServiceDetailKey.PORT, port.toString()),
                                                               ServiceDetail.from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_HEALTH_CHECK_PASSWORD, mongodb_enterprise_health_check_password),
                                                               ServiceDetail.from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_HEALTH_CHECK_USER, mongodb_enterprise_health_check_user)])
                , provisionRequest: new ProvisionRequest(serviceInstanceGuid: 'guid'))
        and:
        def initialAutomationVersion = 1
        1 * context.opsManagerFacade.getAndCheckInitialAutomationGoalVersion(groupId) >> initialAutomationVersion

        and:
        def deployment = new MongoDbEnterpriseDeployment()
        1 * context.opsManagerFacade.deployReplicaSet(groupId, 'guid', port, mongodb_enterprise_health_check_user, mongodb_enterprise_health_check_password, mongodb_version) >> deployment

        when:
        def result = MongoDbEnterpriseProvisionState.REQUEST_AUTOMATION_UPDATE.triggerAction(context)

        then:
        result.go2NextState
        def helper = ServiceDetailsHelper.from(result.details)

        helper.getValue(ServiceDetailKey.DATABASE) == deployment.database
        helper.getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_TARGET_AUTOMATION_GOAL_VERSION) == (initialAutomationVersion + 1).toString()
        helper.getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_REPLICA_SET) == deployment.replicaSet
        helper.getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_MONITORING_AGENT_USER) == deployment.monitoringAgentUser
        helper.getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_MONITORING_AGENT_PASSWORD) == deployment.monitoringAgentPassword
        helper.getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_BACKUP_AGENT_USER) == deployment.backupAgentUser
        helper.getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_BACKUP_AGENT_PASSWORD) == deployment.backupAgentPassword
    }


    def "CHECK_AUTOMATION_UPDATE_STATUS"() {
        given:
        def groupId = 'GroupId'
        def automationVersion = 666
        context.lastOperationJobContext = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: [ServiceDetail.from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_GROUP_ID, groupId),
                                                                                                                     ServiceDetail.from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_TARGET_AUTOMATION_GOAL_VERSION, automationVersion.toString())]))
        and:
        context.opsManagerFacade.isAutomationUpdateComplete(groupId, automationVersion) >> opsManagerResponse

        when:
        def result = MongoDbEnterpriseProvisionState.CHECK_AUTOMATION_UPDATE_STATUS.triggerAction(context)

        then:
        result.go2NextState == go2NextState

        where:
        opsManagerResponse | go2NextState
        true               | true
        false              | false
    }

    def "ENABLE_BACKUP_IF_CONFIGURED"() {
        given:
        def groupId = 'GroupId'
        def replicaSet = 'replicaSet'
        context.lastOperationJobContext = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: [ServiceDetail.from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_GROUP_ID, groupId),
                                                                                                                     ServiceDetail.from(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_REPLICA_SET, replicaSet)]))
        context.mongoDbEnterpriseConfig = new MongoDbEnterpriseConfig(configureDefaultBackupOptions: true, opsManagerIpWhiteList: 'localhost', opsManagerUser: 'user')

        when:
        def result = MongoDbEnterpriseProvisionState.ENABLE_BACKUP_IF_CONFIGURED.triggerAction(context)

        then:
        result.go2NextState
        1 * context.opsManagerFacade.whiteListIpsForUser(context.mongoDbEnterpriseConfig.opsManagerUser, [context.mongoDbEnterpriseConfig.opsManagerIpWhiteList])
        1 * context.opsManagerFacade.enableBackupAndSetStorageEngine(groupId, replicaSet)
        1 * context.opsManagerFacade.updateSnapshotSchedule(groupId, replicaSet)
    }

    def "PROVISION_SUCCESS"() {
        when:
        MongoDbEnterpriseProvisionState.PROVISION_SUCCESS.triggerAction(null)
        then:
        0 * _._
    }
}
