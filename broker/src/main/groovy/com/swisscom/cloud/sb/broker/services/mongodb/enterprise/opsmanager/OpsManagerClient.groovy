package com.swisscom.cloud.sb.broker.services.mongodb.enterprise.opsmanager

import com.google.common.base.Optional
import com.google.gson.Gson
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.MongoDbEnterpriseConfig
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.access.GroupDto
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.access.OpsManagerUserDto
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.automation.*
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate

import static com.google.common.base.Strings.isNullOrEmpty
import static com.swisscom.cloud.sb.broker.error.ErrorCode.MONGODB_OPS_MANAGER_AUTHENTICATION_FAILED
import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.UNAUTHORIZED

@Component
class OpsManagerClient {
    public static final String API_V1_CONTEXT_PATH = '/api/public/v1.0'
    public static final String GROUPS = '/groups'
    public static final String USERS = '/users'
    public static final String BY_NAME = '/byName'
    public static final String HOSTS = '/hosts'
    public static final String AUTOMATION_CONFIG = '/automationConfig'
    public static final String AUTOMATION_STATUS = '/automationStatus'
    public static final String AGENTS = '/agents'
    public static final String BACKUP_CONFIGS = '/backupConfigs'
    public static final String SNAPSHOT_SCHEDULE = '/snapshotSchedule'
    public static final String CLUSTERS = '/clusters'
    public static final String WHITE_LIST = '/whitelist'

    public static final String AUTOMATION = '/AUTOMATION'
    private final RestTemplateBuilder restTemplateBuilder
    private final MongoDbEnterpriseConfig mongoDbEnterpriseConfig

    @Autowired
    OpsManagerClient(RestTemplateBuilder restTemplateBuilder, MongoDbEnterpriseConfig mongoDbEnterpriseConfig) {
        this.restTemplateBuilder = restTemplateBuilder
        this.mongoDbEnterpriseConfig = mongoDbEnterpriseConfig
    }

    List<AutomationAgentDto> listAutomationAgents(String groupId) {
        String automationAgents = createRestTemplate().exchange(groupsUrl(groupId) + AGENTS + AUTOMATION, HttpMethod.GET, HttpEntity.EMPTY, String.class).body
        return new JsonSlurper().parseText(automationAgents).results.collect {
            new AutomationAgentDto(confCount: it.confCount, hostname: it.hostname, stateName: it.stateName, typeName: it.typeName)
        }
    }

    private String groupsUrl(String groupId = null) {
        return baseUrl() + GROUPS + (isNullOrEmpty(groupId) ? '' : ('/' + groupId))
    }

    void updateAutomationConfig(String groupId, AutomationConfigDto automationConfig) {
        createRestTemplate().exchange(groupsUrl(groupId) + AUTOMATION_CONFIG, HttpMethod.PUT, new HttpEntity<AutomationConfigDto>(automationConfig), Void.class)
    }

    AutomationConfigDto getAutomationConfig(String groupId) {
        return createRestTemplate().getForObject(groupsUrl(groupId) + AUTOMATION_CONFIG, AutomationConfigDto.class)
    }

    AutomationStatusDto getAutomationStatus(String groupId) {
        return createRestTemplate().getForObject(groupsUrl(groupId) + AUTOMATION_STATUS, AutomationStatusDto.class)
    }

    //TODO
    GroupDto createGroup(GroupDto groupDto) {
        return createRestTemplate().exchange(groupsUrl(), HttpMethod.POST, new HttpEntity<GroupDto>(groupDto), GroupDto.class).body
    }

    GroupDto getGroup(String groupId) {
        return createRestTemplate().getForObject(groupsUrl(groupId), GroupDto.class)
    }

    void deleteGroup(String groupId) {
        createRestTemplate().delete(groupsUrl(groupId))
    }

    OpsManagerUserDto createUser(OpsManagerUserDto userDto) {
        return createRestTemplate().exchange(usersUrl(), HttpMethod.POST, new HttpEntity<OpsManagerUserDto>(userDto), OpsManagerUserDto.class).body
    }

    private String usersUrl(String userId = null) {
        return baseUrl() + USERS + (isNullOrEmpty(userId) ? '' : ('/' + userId))
    }

    OpsManagerUserDto getUserByName(String name) {
        return createRestTemplate().getForObject(usersByNameUrl(name), OpsManagerUserDto.class)
    }

    private String usersByNameUrl(String userName) {
        return baseUrl() + USERS + BY_NAME + "/" + userName
    }

    WhiteListResultSetDto getUserWhiteList(String userId) {
        return createRestTemplate().getForObject(usersWhiteListUrl(userId), WhiteListResultSetDto.class)
    }

    private String usersWhiteListUrl(String userId) {
        return usersUrl(userId) + WHITE_LIST
    }

    void addUserWhiteList(String userId, List<WhiteListDto> whiteList) {
        createRestTemplate().exchange(usersWhiteListUrl(userId), HttpMethod.POST, new HttpEntity<List<WhiteListDto>>(whiteList), Void.class)
    }

    void deleteUser(String userId) {
        createRestTemplate().delete(usersUrl(userId))
    }

    BackupConfigDto getBackupConfig(String groupId, String clusterId) {
        createRestTemplate().getForObject(backupConfigUrl(groupId, clusterId), BackupConfigDto.class)
    }

    private String backupConfigUrl(String groupId, String clusterId) {
        return groupsUrl(groupId) + BACKUP_CONFIGS + (clusterId ? "/" + clusterId : "")
    }

    BackupConfigDto updateBackupConfig(String groupId, String clusterId, BackupConfigDto data) {
        createRestTemplate().exchange(backupConfigUrl(groupId, clusterId), HttpMethod.PATCH, new HttpEntity<BackupConfigDto>(data), BackupConfigDto.class).body
    }

    void deleteBackupConfig(String groupId, String clusterId) {
        createRestTemplate().delete(backupConfigUrl(groupId, clusterId))
    }

    SnapshotScheduleDto getSnapshotSchedule(String groupId, String clusterId) {
        return createRestTemplate().getForObject(snapshotScheduleUrl(groupId, clusterId), SnapshotScheduleDto.class)
    }

    private String snapshotScheduleUrl(String groupId, String clusterId) {
        return backupConfigUrl(groupId, clusterId) + SNAPSHOT_SCHEDULE
    }

    SnapshotScheduleDto updateSnapshotSchedule(String groupId, String clusterId, SnapshotScheduleDto data) {
        return createRestTemplate().exchange(snapshotScheduleUrl(groupId, clusterId), HttpMethod.PATCH, new HttpEntity<SnapshotScheduleDto>(data),
                SnapshotScheduleDto.class).body
    }

    void deleteSnapshotSchedule(String groupId, String clusterId) {
        createRestTemplate().delete(snapshotScheduleUrl(groupId, clusterId))
    }

    ClustersDto getClusters(String groupId) {
        return createRestTemplate().getForObject(groupsUrl(groupId) + CLUSTERS, ClustersDto.class)
    }

    String getClusterId(String groupId, String replicaSetName) {
        def clusters = getClusters(groupId)
        return clusters.results.find({ it.replicaSetName == replicaSetName }).id
    }

    Optional<String> findClusterId(String groupId, String replicaSetName) {
        def clusters = getClusters(groupId)
        def cluster = clusters.results.find({ it.replicaSetName == replicaSetName })
        return cluster ? Optional.of(cluster.id) : Optional.absent()
    }

    private static <T> T parseJson(String data, Class<T> clazz) {
        return new Gson().fromJson((String) data, clazz)
    }

    def getHostIds(String groupId) {
        String hosts = getHosts(groupId)
        def json = new JsonSlurper().parseText(hosts)
        return json.results.collect { it.id }
    }

    private String getHosts(String groupId) {
        return createRestTemplate().getForEntity(groupsUrl(groupId) + HOSTS, String.class).body
    }

    def deleteHost(String groupId, String hostId) {
        createRestTemplate().delete(groupsUrl(groupId) + "${HOSTS}/${hostId}")
    }


    private RestTemplate createRestTemplate() {
        def restTemplate = restTemplateBuilder.withDigestAuthentication(mongoDbEnterpriseConfig.opsManagerUser, mongoDbEnterpriseConfig.opsManagerApiKey).build()
        restTemplate.setErrorHandler(new CustomErrorHandler())
        return restTemplate
    }

    @Slf4j
    private static class CustomErrorHandler extends DefaultResponseErrorHandler {

        @Override
        void handleError(ClientHttpResponse response) throws IOException {
            if (UNAUTHORIZED == response.statusCode || FORBIDDEN == response.statusCode) {
                MONGODB_OPS_MANAGER_AUTHENTICATION_FAILED.throwNew()
            }
            log.error("Opsmanager call failed, status:${response.statusCode}, statusText:${response.statusText}, body:${response.getBody().text}")
            super.handleError(response)
        }
    }

    private String baseUrl() {
        return mongoDbEnterpriseConfig.opsManagerUrl + API_V1_CONTEXT_PATH
    }
}