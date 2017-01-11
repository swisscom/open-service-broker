package com.swisscom.cf.broker.services.mongodb.enterprise

import com.google.common.base.Optional
import com.google.common.base.Preconditions
import com.swisscom.cf.broker.binding.BindRequest
import com.swisscom.cf.broker.binding.BindResponse
import com.swisscom.cf.broker.binding.UnbindRequest
import com.swisscom.cf.broker.model.ProvisionRequest
import com.swisscom.cf.broker.model.ServiceDetail
import com.swisscom.cf.broker.model.ServiceInstance
import com.swisscom.cf.broker.provisioning.async.AsyncOperationResult
import com.swisscom.cf.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cf.broker.provisioning.statemachine.ServiceStateWithAction
import com.swisscom.cf.broker.provisioning.statemachine.StateMachine
import com.swisscom.cf.broker.provisioning.statemachine.StateMachineContext
import com.swisscom.cf.broker.services.bosh.BoshBasedServiceProvider
import com.swisscom.cf.broker.services.bosh.BoshTemplate
import com.swisscom.cf.broker.services.bosh.statemachine.BoshStateMachineFactory
import com.swisscom.cf.broker.services.mongodb.enterprise.opsmanager.DbUserCredentials
import com.swisscom.cf.broker.services.mongodb.enterprise.opsmanager.OpsManagerFacade
import com.swisscom.cf.broker.services.mongodb.enterprise.statemachine.MongoDbEnterperiseStateMachineContext
import com.swisscom.cf.broker.services.mongodb.enterprise.statemachine.MongoDbEnterpriseDeprovisionState
import com.swisscom.cf.broker.services.mongodb.enterprise.statemachine.MongoDbEnterpriseProvisionState
import com.swisscom.cf.broker.util.ServiceDetailKey
import com.swisscom.cf.broker.util.ServiceDetailType
import com.swisscom.cf.broker.util.ServiceDetailsHelper
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

import static com.google.common.base.Strings.isNullOrEmpty
import static com.swisscom.cf.broker.model.ServiceDetail.from
import static com.swisscom.cf.broker.services.mongodb.enterprise.statemachine.MongoDbEnterpriseProvisionState.*
import static com.swisscom.cf.broker.util.ServiceDetailKey.*

@Component
@CompileStatic
@Log4j
class MongoDbEnterpriseServiceProvider extends BoshBasedServiceProvider<MongoDbEnterpriseConfig> {
    public static final String PARAM_MMS_BASE_URL = "mms-base-url"
    public static final String PARAM_MMS_API_KEY = "mms-api-key"
    public static final String MMS_GROUP_ID = "mms-group-id"

    public static final String PORT = 'port'
    public static final String MONGODB_BINARY_PATH = 'mongodb-binary-path'
    public static final String HEALTH_CHECK_USER = 'health-check-user'
    public static final String HEALTH_CHECK_PASSWORD = 'health-check-password'

    @Autowired
    protected OpsManagerFacade opsManagerFacade

    @Autowired
    protected MongoDbEnterpriseFreePortFinder mongoDbEnterpriseFreePortFinder

    @PostConstruct
    void init() {
        log.info(serviceConfig.toString())
    }

    @Override
    Collection<ServiceDetail> customizeBoshTemplate(BoshTemplate template, ProvisionRequest provisionRequest) {
        ServiceInstance serviceInstance = provisioningPersistenceService.getServiceInstance(provisionRequest.serviceInstanceGuid)

        String opsManagerGroupId = ServiceDetailsHelper.from(serviceInstance.details).getValue(MONGODB_ENTERPRISE_GROUP_ID)
        Preconditions.checkArgument(!isNullOrEmpty(opsManagerGroupId), "A valid OpsManager GroupId is required")

        String agentApiKey = ServiceDetailsHelper.from(serviceInstance.details).getValue(MONGODB_ENTERPRISE_AGENT_API_KEY)
        Preconditions.checkArgument(!isNullOrEmpty(agentApiKey), "A valid AgentApiKey is required")

        template.replace(PARAM_MMS_BASE_URL, getOpsManagerUrl())
        template.replace(PARAM_MMS_API_KEY, agentApiKey)
        template.replace(MMS_GROUP_ID, opsManagerGroupId)
        template.replace(MongoDbEnterpriseServiceProvider.PORT, ServiceDetailsHelper.from(serviceInstance.details).getValue(PORT))
        template.replace(MONGODB_BINARY_PATH, ((MongoDbEnterpriseConfig) serviceConfig).libFolder + '/bin/')
        template.replace(HEALTH_CHECK_USER, ServiceDetailsHelper.from(serviceInstance.details).getValue(MONGODB_ENTERPRISE_HEALTH_CHECK_USER))
        template.replace(HEALTH_CHECK_PASSWORD, ServiceDetailsHelper.from(serviceInstance.details).getValue(MONGODB_ENTERPRISE_HEALTH_CHECK_PASSWORD))


        return [from(MONGODB_ENTERPRISE_TARGET_AGENT_COUNT, template.instanceCount() as String)]
    }

    private String getOpsManagerUrl() {
        def mongodbConfig = (MongoDbEnterpriseConfig) serviceConfig
        return mongodbConfig.opsManagerUrlForAutomationAgent ?: mongodbConfig.opsManagerUrl
    }

    @Override
    AsyncOperationResult requestProvision(LastOperationJobContext context) {
        StateMachine stateMachine = getProvisionStateMachine(context)
        ServiceStateWithAction currentState = getProvisionState(context)
        def actionResult = stateMachine.setCurrentState(currentState,createStateMachineContext())
        return AsyncOperationResult.of(actionResult.go2NextState ? stateMachine.nextState(currentState) : currentState, actionResult.details)
    }

    StateMachineContext createStateMachineContext() {
        return new MongoDbEnterperiseStateMachineContext(opsManagerFacade: opsManagerFacade,boshFacade:getBoshFacade() ,
                                                            boshTemplateCustomizer: this,mongoDbEnterpriseFreePortFinder: mongoDbEnterpriseFreePortFinder)
    }

    private StateMachine getProvisionStateMachine(LastOperationJobContext context) {
        StateMachine stateMachine = new StateMachine([CREATE_OPS_MANAGER_GROUP])
        stateMachine.addAllFromStateMachine(BoshStateMachineFactory.createProvisioningStateFlow(serviceConfig.opestackCreateServerGroup))
        stateMachine.addAll([CHECK_AGENTS, REQUEST_AUTOMATION_UPDATE, CHECK_AUTOMATION_UPDATE_STATUS,ENABLE_BACKUP_IF_CONFIGURED,PROVISION_SUCCESS])
    }

    private ServiceStateWithAction getProvisionState(LastOperationJobContext context) {
        ServiceStateWithAction provisionState = null
        if (!context.lastOperation.internalState) {
            provisionState = MongoDbEnterpriseProvisionState.CREATE_OPS_MANAGER_GROUP
        } else {
            provisionState = MongoDbEnterpriseProvisionState.of(context.lastOperation.internalState)
        }
        return provisionState
    }

    @Override
    Optional<AsyncOperationResult> requestDeprovision(LastOperationJobContext context) {
        StateMachine stateMachine = getDeprovisionStateMachine()
        ServiceStateWithAction currentState = getDeprovisionState(context)
        def actionResult = stateMachine.setCurrentState(currentState,createStateMachineContext())
        return Optional.of(AsyncOperationResult.of(actionResult.go2NextState ? stateMachine.nextState(currentState) : currentState, actionResult.details))
    }

    private StateMachine getDeprovisionStateMachine(){
        StateMachine stateMachine = new StateMachine([MongoDbEnterpriseDeprovisionState.INITIAL,
                                                        MongoDbEnterpriseDeprovisionState.AUTOMATION_UPDATE_REQUESTED,
                                                        MongoDbEnterpriseDeprovisionState.AUTOMATION_UPDATED])
        stateMachine.addAllFromStateMachine(BoshStateMachineFactory.createDeprovisioningStateFlow(serviceConfig.opestackCreateServerGroup))
        stateMachine.addAll([MongoDbEnterpriseDeprovisionState.CLEAN_UP_GROUP,MongoDbEnterpriseDeprovisionState.DEPROVISION_SUCCESS])
    }

    private ServiceStateWithAction getDeprovisionState(LastOperationJobContext context) {
        ServiceStateWithAction deprovisionState = null
        if (!context.lastOperation.internalState) {
            deprovisionState = MongoDbEnterpriseDeprovisionState.INITIAL
        } else {
            deprovisionState = MongoDbEnterpriseDeprovisionState.of(context.lastOperation.internalState)
        }
        return deprovisionState
    }

    @Override
    BindResponse bind(BindRequest request) {
        def database = ServiceDetailsHelper.from(request.serviceInstance.details).getValue(DATABASE)
        def hosts = ServiceDetailsHelper.from(request.serviceInstance.details).findAllWithServiceDetailType(ServiceDetailType.HOST)
        def groupId = getMongoDbGroupId(request.serviceInstance)
        DbUserCredentials dbUserCredentials = opsManagerFacade.createDbUser(groupId, database)
        def opsManagerCredentials = opsManagerFacade.createOpsManagerUser(groupId, request.serviceInstance.guid)

        return new BindResponse(details: [ServiceDetail.from(ServiceDetailKey.USER, dbUserCredentials.username),
                                          ServiceDetail.from(ServiceDetailKey.PASSWORD, dbUserCredentials.password),
                                          ServiceDetail.from(ServiceDetailKey.MONGODB_ENTERPRISE_OPS_MANAGER_USER_NAME, opsManagerCredentials.user),
                                          ServiceDetail.from(ServiceDetailKey.MONGODB_ENTERPRISE_OPS_MANAGER_PASSWORD, opsManagerCredentials.password),
                                          ServiceDetail.from(ServiceDetailKey.MONGODB_ENTERPRISE_OPS_MANAGER_USER_ID, opsManagerCredentials.userId)],
                credentials: new MongoDbEnterpriseBindResponseDto(
                        database: database,
                        username: dbUserCredentials.username,
                        password: dbUserCredentials.password,
                        hosts: hosts,
                        port: ServiceDetailsHelper.from(request.serviceInstance.details).getValue(PORT),
                        opsManagerUrl: getOpsManagerUrl(),
                        opsManagerUser: opsManagerCredentials.user,
                        opsManagerPassword: opsManagerCredentials.password,
                        replicaSet: ServiceDetailsHelper.from(request.serviceInstance.details).getValue(MONGODB_ENTERPRISE_REPLICA_SET)))
    }

    @Override
    void unbind(UnbindRequest request) {
        opsManagerFacade.deleteDbUser(getMongoDbGroupId(request.serviceInstance),
                ServiceDetailsHelper.from(request.binding.details).getValue(ServiceDetailKey.USER),
                ServiceDetailsHelper.from(request.serviceInstance.details).getValue(ServiceDetailKey.DATABASE))
        opsManagerFacade.deleteOpsManagerUser(ServiceDetailsHelper.from(request.binding.details).getValue(ServiceDetailKey.MONGODB_ENTERPRISE_OPS_MANAGER_USER_ID))
    }

    public static String getMongoDbGroupId(LastOperationJobContext context) {
        return getMongoDbGroupId(context.serviceInstance)
    }

    public static String getMongoDbGroupId(ServiceInstance serviceInstance) {
        return ServiceDetailsHelper.from(serviceInstance.details).getValue(MONGODB_ENTERPRISE_GROUP_ID)
    }
}