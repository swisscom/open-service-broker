package com.swisscom.cloud.sb.broker.services.mongodb.enterprise.opsmanager

import com.google.common.base.Optional
import com.google.common.base.Preconditions
import com.google.gson.Gson
import com.swisscom.cloud.sb.broker.services.common.HostPort
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.MongoDbEnterpriseConfig
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.MongoDbEnterpriseDeployment
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.access.GroupDto
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.access.OpsManagerUserDto
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.automation.*
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.commons.lang.NotImplementedException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import static com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.access.OpsManagerUserDto.UserRole.GROUP_MONITORING_ADMIN
import static com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.automation.AuthenticationDto.DbRole.of
import static com.swisscom.cloud.sb.broker.util.StringGenerator.randomAlphaNumeric
import static com.swisscom.cloud.sb.broker.util.StringGenerator.randomAlphaNumericOfLength16

@Component
@Slf4j
@CompileStatic
class OpsManagerFacade {
    public static final String PROCESS_TYPE_MONGOD = "mongod"
    public static final String PROCESS_TYPE_MONGOS = "mongos"
    public static final int LOG_SIZE_THRESHOLD_MB = 1000
    public static final int LOG_TIME_THRESHOLD_HOURS = 24
    public static final String SYSTEM_LOG_DESTINATION = "file"
    public static final String OPTION_DOWNLOAD_BASE = 'downloadBase'
    public static final String AUTH_MECHANISM_MONGODB_CR = "MONGODB-CR"
    public static final String USER_NAME_MMS_MONITORING_AGENT = "mms-monitoring-agent"
    public static final String USER_NAME_MMS_BACKUP_AGENT = "mms-backup-agent"
    public static final String USER_NAME_CLOUD_OPS = "cloudOps"
    public static final String DB_ADMIN = "admin"
    public static final String DB_LOCAL = "local"
    public static final String ROLE_CLUSTER_MONITOR = "clusterMonitor"
    public static final String ROLE_CLUSTER_ADMIN = "clusterAdmin"
    public static final String ROLE_READ_ANY_DATABASE = "readAnyDatabase"
    public static final String ROLE_USER_ADMIN_ANY_DATABASE = "userAdminAnyDatabase"
    public static final String ROLE_READ_WRITE = "readWrite"
    public static final String ROLE_ROOT = "root"
    public static final String STORAGE_ENGINE_WIRED_TIGER = "WIRED_TIGER"
    public static final String SYNC_SOURCE_SECONDARY = "SYNC_SOURCE_SECONDARY"

    public static
    final List<OpsManagerUserDto.UserRole> DEFAULT_OPS_MANAGER_ROLES = Collections.unmodifiableList([GROUP_MONITORING_ADMIN])

    @Autowired
    OpsManagerClient opsManagerClient

    @Autowired
    MongoDbEnterpriseConfig mongoDbEnterpriseConfig

    def OpsManagerGroup createGroup(String serviceInstanceId) {
        GroupDto groupDto = opsManagerClient.createGroup(new GroupDto(name: serviceInstanceId, publicApiEnabled: true))
        return new OpsManagerGroup(groupId: groupDto.id,
                groupName: groupDto.name,
                agentApiKey: groupDto.agentApiKey)
    }

    def deleteGroup(String groupId) {
        opsManagerClient.deleteGroup(groupId)
    }

    def OpsManagerCredentials createOpsManagerUser(String groupId, String serviceInstanceId) {
        def userCreationRequestDto = createUserDto(groupId, serviceInstanceId)
        OpsManagerUserDto userDto = opsManagerClient.createUser(userCreationRequestDto)
        return new OpsManagerCredentials(user: userCreationRequestDto.username,
                password: userCreationRequestDto.password,
                userId: userDto.id)
    }

    def deleteOpsManagerUser(String userId) {
        opsManagerClient.deleteUser(userId)
    }

    private OpsManagerUserDto createUserDto(String groupId, String serviceInstanceId) {
        OpsManagerUserDto userDto = new OpsManagerUserDto(firstName: serviceInstanceId,
                lastName: serviceInstanceId,
                username: randomAlphaNumericOfLength16(),
                password: randomAlphaNumeric(32) + "!1aA",//include a symbol,digit, small & upper case letter in each password
                roles: [])

        getConfiguredOpsManagerUserRoles().each {
            OpsManagerUserDto.UserRole role -> userDto.roles.add(new OpsManagerUserDto.Role(groupId: groupId, roleName: role.toString()))
        }
        return userDto
    }

    private List<OpsManagerUserDto.UserRole> getConfiguredOpsManagerUserRoles() {
        if (mongoDbEnterpriseConfig.opsManagerUserRoles) {
            try {
                List<OpsManagerUserDto.UserRole> result = []
                mongoDbEnterpriseConfig.opsManagerUserRoles.split(",").each({ String role ->
                    result.add(OpsManagerUserDto.UserRole.valueOf(role))
                })
                return result
            } catch (Exception e) {
                log.error("Configured opsManagerUserRoles:(${mongoDbEnterpriseConfig.opsManagerUserRoles}) parsing failed", e)
                return DEFAULT_OPS_MANAGER_ROLES
            }
        } else {
            return DEFAULT_OPS_MANAGER_ROLES
        }
    }

    boolean areAgentsReady(String groupId, int expectedAgentCount) {
        Preconditions.checkArgument(expectedAgentCount > 0, "agentCount:${expectedAgentCount} should be larger than 0")
        def availableHosts = opsManagerClient.listAutomationAgents(groupId).collect { it.hostname }
        if (expectedAgentCount != availableHosts.size()) {
            log.info("Current available agent count:${availableHosts.size()} is different than expected:${expectedAgentCount}")
            return false
        }

        return true
    }

    def MongoDbEnterpriseDeployment deployStandAlone(String groupId, String database, int port) {
        throw new NotImplementedException()
    }

    //TODO refactor this method ,passing in too many arguments
    def MongoDbEnterpriseDeployment deployReplicaSet(String groupId, String database, int port, String healthUser, String healthPassword, String mongoDbVersion) {
        final List<String> hosts = findHostsForGroup(groupId)
        final List<HostPort> hostPorts = hosts.collect { String host -> new HostPort(host: host, port: port) }

        MongoDbEnterpriseDeployment deployment = null
        updateAutomationConfig(groupId, { AutomationConfigDto automationConfigDto ->
            deployment = configureInstancesWithReplicaSetMode(database, hostPorts, automationConfigDto, healthUser, healthPassword, mongoDbVersion)
        })

        return deployment
    }

    def MongoDbEnterpriseDeployment deployShard(String groupId, String database, int port) {
        throw new NotImplementedException()
    }


    private List<String> findHostsForGroup(String groupId) {
        def reservedHosts = opsManagerClient.getAutomationStatus(groupId).processes?.collect { it.hostname }
        Preconditions.checkArgument(reservedHosts == null || reservedHosts.size() == 0, "There should not be any running mongodb processes.")

        return opsManagerClient.listAutomationAgents(groupId).collect { it.hostname }
    }

    private MongoDbEnterpriseDeployment configureInstancesWithReplicaSetMode(String database, List<HostPort> hostPorts, AutomationConfigDto automationConfig,
                                                                             String healthUser, String healthPassword,
                                                                             String mongoDbVersion) {
        final String replicaSetId = "rs_${database}"

        if (!automationConfig.options) {
            automationConfig.options = [:]
        }
        automationConfig.options[OPTION_DOWNLOAD_BASE] = mongoDbEnterpriseConfig.libFolder

        if (automationConfig.processes && automationConfig.processes.size() > 0) {
            throw new IllegalStateException("Processes should be empty")
        }

        automationConfig.processes = []
        hostPorts.eachWithIndex {
            HostPort hostPort, int i ->
                String name = "${replicaSetId}_${i}"
                String path = createDbPath(name)
                ProcessDto processDto = createProcessDto(hostPort, replicaSetId, path, name, mongoDbVersion)
                automationConfig.processes.add(processDto)
        }

        final List<ReplicaSetDto.Member> members = []
        automationConfig.processes.eachWithIndex {
            ProcessDto entry, int i ->
                members.add(new ReplicaSetDto.Member(_id: i,
                        host: entry.name,
                        priority: 1,
                        votes: 1,
                        hidden: false,
                        arbiterOnly: false,
                        slaveDelay: 0))
        }

        if (automationConfig.replicaSets && automationConfig.replicaSets.size() > 0) {
            throw new IllegalStateException("ReplicaSets should be empty")
        }
        automationConfig.replicaSets = [new ReplicaSetDto(_id: replicaSetId, members: members)]

        def deployment = new MongoDbEnterpriseDeployment(database: replicaSetId, replicaSet: replicaSetId, hostPorts: hostPorts,
                monitoringAgentUser: USER_NAME_MMS_MONITORING_AGENT,
                monitoringAgentPassword: randomAlphaNumeric(32),
                backupAgentUser: USER_NAME_MMS_BACKUP_AGENT,
                backupAgentPassword: randomAlphaNumeric(32),
                operationsUser: USER_NAME_CLOUD_OPS,
                operationsPassword: randomAlphaNumeric(32),
                healthUser: healthUser,
                healthPassword: healthPassword)

        //https://github.com/mongodb-labs/mms-api-examples/blob/master/automation/api_usage_example/configs/api_6_enable_auth.json
        //https://docs.opsmanager.mongodb.com/current/reference/cluster-configuration/#authentication

        automationConfig.auth = createInitialAuthenticationDto(deployment)

        automationConfig.monitoringVersions = processMonitoringVersions(automationConfig)
        automationConfig.backupVersions = processBackupVersions(automationConfig)

        return deployment
    }

    private List<MonitoringVersionDto> processMonitoringVersions(AutomationConfigDto automationConfigDto) {
        List<MonitoringVersionDto> result = []
        automationConfigDto.processes.each {
            result.add(new MonitoringVersionDto(hostname: it.hostname,
                    logPath: mongoDbEnterpriseConfig.logFolder + "/monitoring-agent.log",
                    logRotate: createLogRotateDto()))
        }

        return result
    }

    private List<BackupVersionDto> processBackupVersions(AutomationConfigDto automationConfigDto) {
        List<BackupVersionDto> result = []
        automationConfigDto.processes.each {
            result.add(new BackupVersionDto(hostname: it.hostname,
                    logPath: mongoDbEnterpriseConfig.logFolder + "/backup-agent.log",
                    logRotate: createLogRotateDto()))
        }

        return result
    }

    private ProcessDto createProcessDto(HostPort hostPort, String replicaSet, String path, String name, String mongoDbVersion) {
        ProcessDto processDto = new ProcessDto(version: mongoDbVersion,
                processType: PROCESS_TYPE_MONGOD,
                name: name,
                authSchemaVersion: mongoDbEnterpriseConfig.authSchemaVersion,
                featureCompatibilityVersion: mongoDbEnterpriseConfig.featureCompatibilityVersion,
                hostname: hostPort.host,
                logRotate: createLogRotateDto(),
                args2_6: new ProcessArgumentsV26Dto(net: new ProcessArgumentsV26Dto.Net(port: hostPort.port),
                        storage: new ProcessArgumentsV26Dto.Storage(dbPath: path),
                        systemLog: new ProcessArgumentsV26Dto.SystemLog(destination: SYSTEM_LOG_DESTINATION, path: "${path}/mongodb.log"),
                        replication: new ProcessArgumentsV26Dto.Replication(replSetName: replicaSet)))
        return processDto
    }

    private LogRotateDto createLogRotateDto() {
        return new LogRotateDto(sizeThresholdMB: LOG_SIZE_THRESHOLD_MB, timeThresholdHrs: LOG_TIME_THRESHOLD_HOURS)
    }

    private String createDbPath(String database) {
        return mongoDbEnterpriseConfig.dbFolder + (mongoDbEnterpriseConfig.dbFolder.endsWith('/') ? '' : '/') + database
    }

    private AuthenticationDto createInitialAuthenticationDto(MongoDbEnterpriseDeployment deployment) {
        def auth = new AuthenticationDto()
        auth.with {
            disabled = false
            autoUser = 'mms-automation'
            autoPwd = randomAlphaNumericOfLength16()
            autoAuthMechanism = AUTH_MECHANISM_MONGODB_CR
            usersWanted = [
                    createUser(DB_ADMIN, deployment.monitoringAgentUser, deployment.monitoringAgentPassword, [of(DB_ADMIN, ROLE_CLUSTER_MONITOR)]),
                    createUser(DB_ADMIN, deployment.backupAgentUser, deployment.backupAgentPassword, [of(DB_ADMIN, ROLE_CLUSTER_ADMIN),
                                                                                                      of(DB_ADMIN, ROLE_READ_ANY_DATABASE),
                                                                                                      of(DB_ADMIN, ROLE_USER_ADMIN_ANY_DATABASE),
                                                                                                      of(DB_ADMIN, ROLE_READ_WRITE),
                                                                                                      of(DB_LOCAL, ROLE_READ_WRITE)]),
                    createUser(DB_ADMIN, deployment.operationsUser, deployment.operationsPassword, [of(DB_ADMIN, ROLE_ROOT)])
            ]

            if (deployment.healthUser && deployment.healthPassword) {
                usersWanted.add(createUser(DB_ADMIN, deployment.healthUser, deployment.healthPassword, [of(DB_ADMIN, ROLE_CLUSTER_ADMIN)]))
            }
        }

        populateKeyInfo(auth)

        return auth
    }

    private AuthenticationDto.DbUser createUser(String db, String username, String password, List<AuthenticationDto.DbRole> roles) {
        def user = new AuthenticationDto.DbUser(user: username, db: db, initPwd: password)
        user.roles = roles
        return user
    }


    DbUserCredentials createDbUser(String groupdId, String database) {
        String username = randomAlphaNumericOfLength16()
        String password = randomAlphaNumericOfLength16()

        getAutomationConfigAndApplyClosure(groupdId, {
            AutomationConfigDto automationConfigDto ->
                addUserToDatabase(automationConfigDto.auth, username, password, database)
                setAuthMechamnismIfNotAlreadySet(automationConfigDto.auth)
        })

        return new DbUserCredentials(username: username, password: password)
    }

    void deleteDbUser(String groupId, String user, String database) {
        getAutomationConfigAndApplyClosure(groupId, {
            AutomationConfigDto automationConfigDto ->
                def users2Delete = automationConfigDto.auth.usersWanted.findAll({ it.user == user })
                if (users2Delete) {
                    automationConfigDto.auth.usersWanted.removeAll(users2Delete)
                }
                if (!automationConfigDto.auth.usersDeleted) {
                    automationConfigDto.auth.usersDeleted = []
                }
                automationConfigDto.auth.usersDeleted.add(new AuthenticationDto.DbUser2Delete(user: user, dbs: [database]))
                setAuthMechamnismIfNotAlreadySet(automationConfigDto.auth)
        })
    }

    private void populateKeyInfo(AuthenticationDto authenticationDto) {
        authenticationDto.keyfile = mongoDbEnterpriseConfig.libFolder + '/keyfile'
        authenticationDto.key = randomAlphaNumeric(128)
    }

    private void addUserToDatabase(AuthenticationDto authenticationDto, String username, String password, String database) {
        authenticationDto.usersWanted.add(createUser(database, username, password, [of(database, ROLE_READ_WRITE)]))
        //populateKeyInfo(authenticationDto)
    }

    private void setAuthMechamnismIfNotAlreadySet(AuthenticationDto authenticationDto){
        if(!authenticationDto.autoAuthMechanism){
            authenticationDto.autoAuthMechanism = AUTH_MECHANISM_MONGODB_CR
        }
    }

    private void updateAutomationConfig(String groupdId, Closure configUpdater) {
        int automationGoalVersionBeforeUpdate = opsManagerClient.getAutomationStatus(groupdId).goalVersion
        Preconditions.checkState(isAutomationUpdateComplete(groupdId, automationGoalVersionBeforeUpdate),
                'The automation update agents are in an inconsistent state!')

        getAutomationConfigAndApplyClosure(groupdId, configUpdater)
    }

    public int getAndCheckInitialAutomationGoalVersion(String groupId) {
        int automationGoalVersion = opsManagerClient.getAutomationStatus(groupId).goalVersion
        Preconditions.checkState(isAutomationUpdateComplete(groupId, automationGoalVersion),
                'The automation update agents are in an inconsistent state!')
        return automationGoalVersion
    }

    private void getAutomationConfigAndApplyClosure(String groupId, Closure configUpdater) {
        AutomationConfigDto automationConfig = opsManagerClient.getAutomationConfig(groupId)


        def jsonBefore = new Gson().toJson(automationConfig)
        log.debug("Received AutomationConfig json:${jsonBefore}")

        configUpdater(automationConfig)

        def jsonAfter = new Gson().toJson(automationConfig)
        log.debug("Modified AutomationConfig json:${jsonAfter}")

        if (jsonAfter.equals(jsonBefore)) {
            log.warn('AutomationConfig before and after closure application are the same, skipping update!')
            return
        }
        opsManagerClient.updateAutomationConfig(groupId, automationConfig)
    }

    public boolean isAutomationUpdateComplete(String groupId, int targetGoalVersion) {
        def automationStatus = opsManagerClient.getAutomationStatus(groupId)
        return automationStatus.goalVersion == targetGoalVersion && haveAllProcessesFinishedUpdating(automationStatus)
    }

    public boolean isAutomationUpdateComplete(String groupId) {
        return haveAllProcessesFinishedUpdating(opsManagerClient.getAutomationStatus(groupId))
    }

    private boolean haveAllProcessesFinishedUpdating(AutomationStatusDto automationStatusDto) {
        for (AutomationStatusDto.Process p : automationStatusDto.processes) {
            if (automationStatusDto.goalVersion != p.lastGoalVersionAchieved) {
                return false
            }
        }
        return true
    }

    void undeploy(String groupdId) {
        getAutomationConfigAndApplyClosure(groupdId, {
            AutomationConfigDto automationConfigDto ->
                automationConfigDto.processes = []
                automationConfigDto.replicaSets = []
                setAuthMechamnismIfNotAlreadySet(automationConfigDto.auth)
        })
    }

    void deleteAllHosts(String groupId) {
        def hostIds = opsManagerClient.getHostIds(groupId)
        hostIds.each { String hostId ->
            opsManagerClient.deleteHost(groupId, hostId)
        }
    }

    BackupConfigDto enableBackupAndSetStorageEngine(String groupId, String replicaSetName) {
        updateBackupConfig(groupId, replicaSetName, new BackupConfigDto(statusName: BackupConfigDto.Status.STARTED.toString(),
                storageEngineName: STORAGE_ENGINE_WIRED_TIGER,
                syncSource: SYNC_SOURCE_SECONDARY))
    }

    BackupConfigDto disableBackup(String groupId, String replicaSetName) {
        log.info("Disabling backup groupId:${groupId}, replicaset:${replicaSetName}")
        updateBackupConfigStatus(groupId, replicaSetName, BackupConfigDto.Status.STOPPED)
    }

    BackupConfigDto terminateBackup(String groupId, String replicaSetName) {
        updateBackupConfigStatus(groupId, replicaSetName, BackupConfigDto.Status.TERMINATING)
    }

    void disableAndTerminateBackup(String groupId, String replicaSet) {
        Optional<String> optionalClusterId = opsManagerClient.findClusterId(groupId, replicaSet)
        if (!optionalClusterId.present) {
            log.info("ClusterId not found for group:${groupId} and replicaSet:${replicaSet}")
            return
        }

        if (isBackupInStartedState(groupId, replicaSet)) {
            log.info("Backup config for group:${groupId} and replicaSet:${replicaSet}, is in STARTED state.")
            BackupConfigDto result = disableBackup(groupId, replicaSet)
            log.info("Backup config for group:${groupId} and replicaSet:${replicaSet}, after disabling is in ${result.statusName} state.")
        }

        if (isBackupInStoppedState(groupId, replicaSet)) {
            BackupConfigDto result = terminateBackup(groupId, replicaSet)
            log.info("Backup config for group:${groupId} and replicaSet:${replicaSet}, after terminating is in ${result.statusName} state.")
        } else if (isBackupInTerminatingState(groupId, replicaSet)) {
            log.info("Backup config for group:${groupId} and replicaSet:${replicaSet}, is in TERMINATING state.")
        }
    }

    private boolean isBackupInStartedState(String groupId, String replicaSet) {
        return isBackupInState(groupId, replicaSet, BackupConfigDto.Status.STARTED)
    }

    private boolean isBackupInStoppedState(String groupId, String replicaSet) {
        return isBackupInState(groupId, replicaSet, BackupConfigDto.Status.STOPPED)
    }

    private boolean isBackupInTerminatingState(String groupId, String replicaSet) {
        return isBackupInState(groupId, replicaSet, BackupConfigDto.Status.TERMINATING)
    }

    private boolean isBackupInState(String groupId, String replicaSet, BackupConfigDto.Status status) {
        def backupConfig = getBackupConfig(groupId, replicaSet)
        log.info("Backup config for group:${groupId} and replicaSet:${replicaSet} is: ${backupConfig}")
        return backupConfig.statusName == status.toString()
    }

    BackupConfigDto getBackupConfig(String groupId, String replicaSet) {
        return opsManagerClient.getBackupConfig(groupId, opsManagerClient.getClusterId(groupId, replicaSet))
    }

    private BackupConfigDto updateBackupConfigStatus(String groupId, String replicaSetName, BackupConfigDto.Status status) {
        updateBackupConfig(groupId, replicaSetName, new BackupConfigDto(statusName: status.toString()))
    }

    private BackupConfigDto updateBackupConfig(String groupId, String replicaSetName, BackupConfigDto backupConfigDto) {
        def clusterId = opsManagerClient.getClusterId(groupId, replicaSetName)
        def result = opsManagerClient.updateBackupConfig(groupId, clusterId, backupConfigDto)
        log.trace("Backup updated for GroupId:${groupId}, ReplicaSet:${replicaSetName}")
        return result
    }


    void updateSnapshotSchedule(String groupId, String replicaSetName) {
        def clusterId = opsManagerClient.getClusterId(groupId, replicaSetName)
        opsManagerClient.updateSnapshotSchedule(groupId, clusterId, createSnapshotScheduleDtoBasedOnConfig())
    }

    private SnapshotScheduleDto createSnapshotScheduleDtoBasedOnConfig() {
        return new SnapshotScheduleDto(
                snapshotIntervalHours: mongoDbEnterpriseConfig.snapshotIntervalHours,
                snapshotRetentionDays: mongoDbEnterpriseConfig.snapshotRetentionDays,
                dailySnapshotRetentionDays: mongoDbEnterpriseConfig.dailySnapshotRetentionDays,
                weeklySnapshotRetentionWeeks: mongoDbEnterpriseConfig.weeklySnapshotRetentionWeeks,
                monthlySnapshotRetentionMonths: mongoDbEnterpriseConfig.monthlySnapshotRetentionMonths,
                pointInTimeWindowHours: mongoDbEnterpriseConfig.pointInTimeWindowHours
        )
    }

    void whiteListIpsForUser(String username, List<String> ipList) {
        def userId = opsManagerClient.getUserByName(username)
        List<WhiteListDto> whiteList = ipList.collect { new WhiteListDto(ipAddress: it.toString()) }
        opsManagerClient.addUserWhiteList(userId.id, whiteList)
    }

}