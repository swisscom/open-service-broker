package com.swisscom.cloud.sb.broker.services.mongodb.enterprise

import com.google.common.base.Optional
import com.swisscom.cf.broker.services.mongodb.enterprise.dto.automation.*
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.access.GroupDto
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.access.OpsManagerUserDto
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.automation.*
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.opsmanager.OpsManagerClient
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.opsmanager.OpsManagerFacade
import spock.lang.Specification

class OpsManagerFacadeSpec extends Specification {
    OpsManagerFacade opsManagerFacade
    OpsManagerClient opsManagerClient
    MongoDbEnterpriseConfig mongoDbEnterpriseConfig

    def groupId = "groupId"
    def database = "database"
    def serviceInstanceId = "serviceInstanceId"

    def setup() {
        mongoDbEnterpriseConfig = new MongoDbEnterpriseConfig(portRange: '27000',
                dbFolder: '/data',
                authSchemaVersion: 5,
                mongoDbVersion: '3.0.7')
        and:
        opsManagerClient = Mock(OpsManagerClient)

        and:
        opsManagerFacade = new OpsManagerFacade(opsManagerClient: opsManagerClient,
                mongoDbEnterpriseConfig: mongoDbEnterpriseConfig)
    }

    def "creation of group functions correctly"() {
        when:
        def result = opsManagerFacade.createGroup(serviceInstanceId)
        then:
        1 * opsManagerClient.createGroup({ GroupDto dto -> dto.name == serviceInstanceId && dto.publicApiEnabled }) >> new GroupDto(id: 'groupId', name: serviceInstanceId, agentApiKey: 'apiKey')
        result.groupId == 'groupId'
        result.groupName == serviceInstanceId
        result.agentApiKey == 'apiKey'
    }

    def "group deletion functions correctly"() {
        when:
        opsManagerFacade.deleteGroup('groupId')
        then:
        1 * opsManagerClient.deleteGroup('groupId')
    }

    def "creation of user functions correctly"() {
        when:
        def result = opsManagerFacade.createOpsManagerUser('groupId', serviceInstanceId)

        then:
        1 * opsManagerClient.createUser({ OpsManagerUserDto dto -> dto.firstName == serviceInstanceId && dto.lastName == serviceInstanceId }) >> new OpsManagerUserDto(id: 'userId')
        result.userId == 'userId'
        result.user
        result.password
    }

    def "opsmanager user roles are set correctly when configuration is empty or null or contains unknown roles"() {
        given:
        mongoDbEnterpriseConfig.opsManagerUserRoles = role
        when:
        opsManagerFacade.createOpsManagerUser('groupId', serviceInstanceId)

        then:
        1 * opsManagerClient.createUser({ OpsManagerUserDto dto ->
            dto.roles.size() == 1 & dto.roles.first().groupId == 'groupId' && dto.roles.first().roleName == OpsManagerFacade.DEFAULT_OPS_MANAGER_ROLES.first().toString()
        }) >> new OpsManagerUserDto(id: 'userId')

        where:
        role << ['', null, 'ThereIsNoSuchOrgManagerRole',
                 "${OpsManagerUserDto.UserRole.GROUP_OWNER.toString()},ThereIsNoSuchOrgManagerRole"]
    }

    def "opsmanager user roles are set correctly when configuration is valid"() {
        given:
        mongoDbEnterpriseConfig.opsManagerUserRoles = roleAsString

        when:
        opsManagerFacade.createOpsManagerUser('groupId', serviceInstanceId)

        then:
        1 * opsManagerClient.createUser({ OpsManagerUserDto dto ->
            dto.roles.size() == givenRoles.size() &&
                    dto.roles.every { givenRoles.collect { it.toString() }.contains(it.roleName) }
        }) >> new OpsManagerUserDto(id: 'userId')

        where:
        roleAsString                                                                                                    | givenRoles
        OpsManagerUserDto.UserRole.GROUP_OWNER.toString()                                                               | [OpsManagerUserDto.UserRole.GROUP_OWNER]
        "${OpsManagerUserDto.UserRole.GROUP_OWNER.toString()},${OpsManagerUserDto.UserRole.GROUP_READ_ONLY.toString()}" | [OpsManagerUserDto.UserRole.GROUP_OWNER, OpsManagerUserDto.UserRole.GROUP_READ_ONLY]
    }

    def "user deletion functions correctly"() {
        when:
        opsManagerFacade.deleteOpsManagerUser('userId')
        then:
        1 * opsManagerClient.deleteUser('userId')
    }

    def "agent check functions correctly"() {
        when:
        opsManagerFacade.areAgentsReady(groupId, 3)

        then:
        1 * opsManagerClient.listAutomationAgents(groupId) >> {
            (1..3).collect { new AutomationAgentDto(hostname: 'host' + it) }
        }
    }

    def "checking initial automation version works correctly"() {
        given:
        opsManagerClient.getAutomationStatus(groupId) >> new AutomationStatusDto(goalVersion: 2, processes: [new AutomationStatusDto.Process(lastGoalVersionAchieved: 2)])
        when:
        def result = opsManagerFacade.getAndCheckInitialAutomationGoalVersion(groupId)
        then:
        result == 2
    }

    def "automation update checking functions correctly"() {
        given:
        opsManagerClient.getAutomationStatus(groupId) >> new AutomationStatusDto(goalVersion: 2, processes: [new AutomationStatusDto.Process(lastGoalVersionAchieved: 2)])
        expect:
        opsManagerFacade.isAutomationUpdateComplete(groupId, 2)
    }

    def "deployment works correctly"() {
        given:
        def config = new AutomationConfigDto()
        opsManagerClient.getAutomationConfig(groupId) >> config
        and:
        opsManagerClient.getAutomationStatus(groupId) >> new AutomationStatusDto(processes: [])
        and:
        opsManagerClient.listAutomationAgents(groupId) >> [new AutomationAgentDto(hostname: 'host')]

        when:
        def result = opsManagerFacade.deployReplicaSet(groupId, database, 27000)

        then:
        1 * opsManagerClient.updateAutomationConfig(groupId, config)
        result.database
    }

    def "host deletion functions correctly"() {
        given:
        def Ids = ["id1", "id2", "id3"]
        1 * opsManagerClient.getHostIds(groupId) >> Ids
        when:
        opsManagerFacade.deleteAllHosts(groupId)
        then:
        Ids.each {
            1 * opsManagerClient.deleteHost(groupId, it)
        }
    }

    def "enableBackupAndSetStorageEngine functions correctly"() {
        given:
        def groupId = 'groupId'
        def replicaSet = 'replicaSet'
        def clusterId = 'clusterId'
        and:
        opsManagerClient.getClusterId(groupId, replicaSet) >> clusterId
        when:
        opsManagerFacade.enableBackupAndSetStorageEngine(groupId, replicaSet)
        then:
        1 * opsManagerClient.updateBackupConfig(groupId, clusterId, { BackupConfigDto d ->
            d.statusName == BackupConfigDto.Status.STARTED.toString() &&
                    d.storageEngineName == OpsManagerFacade.STORAGE_ENGINE_WIRED_TIGER &&
                    d.syncSource == OpsManagerFacade.SYNC_SOURCE_SECONDARY
        })
    }

    def "terminate backup functions correctly"() {
        given:
        def groupId = 'groupId'
        def replicaSet = 'replicaSet'
        def clusterId = 'clusterId'
        and:
        opsManagerClient.getClusterId(groupId, replicaSet) >> clusterId
        when:
        opsManagerFacade.terminateBackup(groupId, replicaSet)
        then:
        1 * opsManagerClient.updateBackupConfig(groupId, clusterId, { BackupConfigDto d -> d.statusName == BackupConfigDto.Status.TERMINATING.toString() })
    }

    def "disable backup functions correctly"() {
        given:
        def groupId = 'groupId'
        def replicaSet = 'replicaSet'
        def clusterId = 'clusterId'
        and:
        opsManagerClient.getClusterId(groupId, replicaSet) >> clusterId

        when:
        opsManagerFacade.disableBackup(groupId, replicaSet)

        then:
        1 * opsManagerClient.updateBackupConfig(groupId, clusterId, { BackupConfigDto d -> d.statusName == BackupConfigDto.Status.STOPPED.toString() })
    }


    def "disableAndTerminateBackup functions correctly when no clusterIsFound"() {
        given:
        def groupId = 'groupId'
        def replicaSet = 'replicaSet'

        when:
        opsManagerFacade.disableAndTerminateBackup(groupId, replicaSet)

        then:
        1 * opsManagerClient.findClusterId(groupId, replicaSet) >> Optional.absent()
        0 * _
    }

    def "snapshot schedule update functions correctly"() {
        given:
        def groupId = 'groupId'
        1 * opsManagerFacade.opsManagerClient.getClusterId(groupId, 'replicaSet') >> 'clusterId'

        when:
        opsManagerFacade.updateSnapshotSchedule(groupId, 'replicaSet')

        then:
        1 * opsManagerFacade.opsManagerClient.updateSnapshotSchedule(groupId, 'clusterId', {
            SnapshotScheduleDto dto ->
                dto.snapshotIntervalHours == opsManagerFacade.mongoDbEnterpriseConfig.snapshotIntervalHours &&
                        dto.snapshotRetentionDays == opsManagerFacade.mongoDbEnterpriseConfig.snapshotRetentionDays &&
                        dto.dailySnapshotRetentionDays == opsManagerFacade.mongoDbEnterpriseConfig.dailySnapshotRetentionDays &&
                        dto.weeklySnapshotRetentionWeeks == opsManagerFacade.mongoDbEnterpriseConfig.weeklySnapshotRetentionWeeks &&
                        dto.monthlySnapshotRetentionMonths == opsManagerFacade.mongoDbEnterpriseConfig.monthlySnapshotRetentionMonths &&
                        dto.pointInTimeWindowHours == opsManagerFacade.mongoDbEnterpriseConfig.pointInTimeWindowHours
        })
    }

    def "user IP white listing functions correctly"() {
        given:
        opsManagerFacade.mongoDbEnterpriseConfig.opsManagerUser = 'userName'

        and:
        1 * opsManagerClient.getUserByName(opsManagerFacade.mongoDbEnterpriseConfig.opsManagerUser) >> new OpsManagerUserDto(id: 'userId')

        when:
        opsManagerFacade.whiteListIpsForUser(opsManagerFacade.mongoDbEnterpriseConfig.opsManagerUser, ['ip1'])

        then:
        1 * opsManagerClient.addUserWhiteList('userId', { List<WhiteListDto> it -> it.size() == 1 && it.first().ipAddress == 'ip1' })
    }

    def "automation update functions correctly when current and to-be-updated configurations are same "() {
        given:
        opsManagerClient.getAutomationConfig(groupId) >> new AutomationConfigDto(processes: [], replicaSets: [])

        when:
        opsManagerFacade.undeploy(groupId)

        then:
        0 * opsManagerClient.updateAutomationConfig(_, _)
    }

    def "automation update functions correctly when current and to-be-updated configurations are different"() {
        given:
        opsManagerClient.getAutomationConfig(groupId) >> new AutomationConfigDto(processes: [new ProcessDto()], replicaSets: [])

        when:
        opsManagerFacade.undeploy(groupId)

        then:
        1 * opsManagerClient.updateAutomationConfig(groupId, _)
    }
}

