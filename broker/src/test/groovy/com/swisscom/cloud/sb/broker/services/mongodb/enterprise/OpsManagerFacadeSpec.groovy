package com.swisscom.cloud.sb.broker.services.mongodb.enterprise

import com.google.common.base.Optional
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

    final String GROUP_ID = "groupId"
    final String REPLICA_SET = "replicaSet"
    final String CLUSTER_ID = "clusterId"
    final String USER_ID = "userId"

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
        1 * opsManagerClient.createGroup({ GroupDto dto -> dto.name == serviceInstanceId && dto.publicApiEnabled }) >> new GroupDto(id: GROUP_ID, name: serviceInstanceId, agentApiKey: 'apiKey')
        result.groupId == GROUP_ID
        result.groupName == serviceInstanceId
        result.agentApiKey == 'apiKey'
    }

    def "group deletion functions correctly"() {
        when:
        opsManagerFacade.deleteGroup(GROUP_ID)
        then:
        1 * opsManagerClient.deleteGroup(GROUP_ID)
    }

    def "creation of user functions correctly"() {
        when:
        def result = opsManagerFacade.createOpsManagerUser(GROUP_ID, serviceInstanceId)

        then:
        1 * opsManagerClient.createUser({ OpsManagerUserDto dto -> dto.firstName == serviceInstanceId && dto.lastName == serviceInstanceId }) >> new OpsManagerUserDto(id: USER_ID)
        result.userId == USER_ID
        result.user
        result.password
    }

    def "opsmanager user roles are set correctly when configuration is empty or null or contains unknown roles"() {
        given:
        mongoDbEnterpriseConfig.opsManagerUserRoles = role
        when:
        opsManagerFacade.createOpsManagerUser(GROUP_ID, serviceInstanceId)

        then:
        1 * opsManagerClient.createUser({ OpsManagerUserDto dto ->
            dto.roles.size() == 1 & dto.roles.first().groupId == GROUP_ID && dto.roles.first().roleName == OpsManagerFacade.DEFAULT_OPS_MANAGER_ROLES.first().toString()
        }) >> new OpsManagerUserDto(id: USER_ID)

        where:
        role << ['', null, 'ThereIsNoSuchOrgManagerRole',
                 "${OpsManagerUserDto.UserRole.GROUP_OWNER.toString()},ThereIsNoSuchOrgManagerRole"]
    }

    def "opsmanager user roles are set correctly when configuration is valid"() {
        given:
        mongoDbEnterpriseConfig.opsManagerUserRoles = roleAsString

        when:
        opsManagerFacade.createOpsManagerUser(GROUP_ID, serviceInstanceId)

        then:
        1 * opsManagerClient.createUser({ OpsManagerUserDto dto ->
            dto.roles.size() == givenRoles.size() &&
                    dto.roles.every { givenRoles.collect { it.toString() }.contains(it.roleName) }
        }) >> new OpsManagerUserDto(id: USER_ID)

        where:
        roleAsString                                                                                                    | givenRoles
        OpsManagerUserDto.UserRole.GROUP_OWNER.toString()                                                               | [OpsManagerUserDto.UserRole.GROUP_OWNER]
        "${OpsManagerUserDto.UserRole.GROUP_OWNER.toString()},${OpsManagerUserDto.UserRole.GROUP_READ_ONLY.toString()}" | [OpsManagerUserDto.UserRole.GROUP_OWNER, OpsManagerUserDto.UserRole.GROUP_READ_ONLY]
    }

    def "user deletion functions correctly"() {
        when:
        opsManagerFacade.deleteOpsManagerUser(USER_ID)
        then:
        1 * opsManagerClient.deleteUser(USER_ID)
    }

    def "agent check functions correctly"() {
        when:
        opsManagerFacade.areAgentsReady(GROUP_ID, 3)

        then:
        1 * opsManagerClient.listAutomationAgents(GROUP_ID) >> {
            (1..3).collect { new AutomationAgentDto(hostname: 'host' + it) }
        }
    }

    def "checking initial automation version works correctly"() {
        given:
        opsManagerClient.getAutomationStatus(GROUP_ID) >> new AutomationStatusDto(goalVersion: 2, processes: [new AutomationStatusDto.Process(lastGoalVersionAchieved: 2)])
        when:
        def result = opsManagerFacade.getAndCheckInitialAutomationGoalVersion(GROUP_ID)
        then:
        result == 2
    }

    def "automation update checking functions correctly"() {
        given:
        opsManagerClient.getAutomationStatus(GROUP_ID) >> new AutomationStatusDto(goalVersion: 2, processes: [new AutomationStatusDto.Process(lastGoalVersionAchieved: 2)])
        expect:
        opsManagerFacade.isAutomationUpdateComplete(GROUP_ID, 2)
    }

    def "deployment works correctly"() {
        given:
        def config = new AutomationConfigDto()
        opsManagerClient.getAutomationConfig(GROUP_ID) >> config
        and:
        opsManagerClient.getAutomationStatus(GROUP_ID) >> new AutomationStatusDto(processes: [])
        and:
        opsManagerClient.listAutomationAgents(GROUP_ID) >> [new AutomationAgentDto(hostname: 'host')]

        when:
        def result = opsManagerFacade.deployReplicaSet(GROUP_ID, database, 27000, 'healthuser', 'healthpassword', 'version')

        then:
        1 * opsManagerClient.updateAutomationConfig(GROUP_ID, config)
        result.database
    }

    def "host deletion functions correctly"() {
        given:
        def Ids = ["id1", "id2", "id3"]
        1 * opsManagerClient.getHostIds(GROUP_ID) >> Ids
        when:
        opsManagerFacade.deleteAllHosts(GROUP_ID)
        then:
        Ids.each {
            1 * opsManagerClient.deleteHost(GROUP_ID, it)
        }
    }

    def "enableBackupAndSetStorageEngine functions correctly"() {
        given:
        opsManagerClient.getClusterId(GROUP_ID, REPLICA_SET) >> CLUSTER_ID
        when:
        opsManagerFacade.enableBackupAndSetStorageEngine(GROUP_ID, REPLICA_SET)
        then:
        1 * opsManagerClient.updateBackupConfig(GROUP_ID, CLUSTER_ID, { BackupConfigDto d ->
            d.statusName == BackupConfigDto.Status.STARTED.toString() &&
                    d.storageEngineName == OpsManagerFacade.STORAGE_ENGINE_WIRED_TIGER &&
                    d.syncSource == OpsManagerFacade.SYNC_SOURCE_SECONDARY
        })
    }

    def "terminate backup functions correctly"() {
        given:
        opsManagerClient.getClusterId(GROUP_ID, REPLICA_SET) >> CLUSTER_ID
        when:
        opsManagerFacade.terminateBackup(GROUP_ID, REPLICA_SET)
        then:
        1 * opsManagerClient.updateBackupConfig(GROUP_ID, CLUSTER_ID, { BackupConfigDto d -> d.statusName == BackupConfigDto.Status.TERMINATING.toString() })
    }

    def "disable backup functions correctly"() {
        given:
        opsManagerClient.getClusterId(GROUP_ID, REPLICA_SET) >> CLUSTER_ID

        when:
        opsManagerFacade.disableBackup(GROUP_ID, REPLICA_SET)

        then:
        1 * opsManagerClient.updateBackupConfig(GROUP_ID, CLUSTER_ID, { BackupConfigDto d -> d.statusName == BackupConfigDto.Status.STOPPED.toString() })
    }


    def "disableAndTerminateBackup functions correctly when no clusterIsFound"() {
        when:
        opsManagerFacade.disableAndTerminateBackup(GROUP_ID, REPLICA_SET)

        then:
        1 * opsManagerClient.findClusterId(GROUP_ID, REPLICA_SET) >> Optional.absent()
        0 * _
    }

    def "snapshot schedule update functions correctly"() {
        given:
        1 * opsManagerFacade.opsManagerClient.getClusterId(GROUP_ID, REPLICA_SET) >> CLUSTER_ID

        when:
        opsManagerFacade.updateSnapshotSchedule(GROUP_ID, REPLICA_SET)

        then:
        1 * opsManagerFacade.opsManagerClient.updateSnapshotSchedule(GROUP_ID, CLUSTER_ID, {
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
        1 * opsManagerClient.getUserByName(opsManagerFacade.mongoDbEnterpriseConfig.opsManagerUser) >> new OpsManagerUserDto(id: USER_ID)

        when:
        opsManagerFacade.whiteListIpsForUser(opsManagerFacade.mongoDbEnterpriseConfig.opsManagerUser, ['ip1'])

        then:
        1 * opsManagerClient.addUserWhiteList(USER_ID, { List<WhiteListDto> it -> it.size() == 1 && it.first().ipAddress == 'ip1' })
    }

    def "automation update functions correctly when current and to-be-updated configurations are same "() {
        given:
        opsManagerClient.getAutomationConfig(GROUP_ID) >> new AutomationConfigDto(processes: [], replicaSets: [], auth: new AuthenticationDto(autoAuthMechanism: "MONGODB-CR"))

        when:
        opsManagerFacade.undeploy(GROUP_ID)

        then:
        0 * opsManagerClient.updateAutomationConfig(_, _)
    }

    def "automation update functions correctly when current and to-be-updated configurations are different"() {
        given:
        opsManagerClient.getAutomationConfig(GROUP_ID) >> new AutomationConfigDto(processes: [new ProcessDto()], replicaSets: [], auth: new AuthenticationDto(autoAuthMechanism: "MONGODB-CR"))

        when:
        opsManagerFacade.undeploy(GROUP_ID)

        then:
        1 * opsManagerClient.updateAutomationConfig(GROUP_ID, _)
    }
}

