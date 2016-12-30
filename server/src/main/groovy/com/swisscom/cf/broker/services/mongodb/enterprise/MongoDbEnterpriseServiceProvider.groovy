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
import com.swisscom.cf.broker.provisioning.statemachine.*
import com.swisscom.cf.broker.provisioning.statemachine.action.NoOp
import com.swisscom.cf.broker.services.bosh.*
import com.swisscom.cf.broker.services.common.DnsBasedStatusCheck
import com.swisscom.cf.broker.services.mongodb.enterprise.opsmanager.DbUserCredentials
import com.swisscom.cf.broker.services.mongodb.enterprise.opsmanager.OpsManagerFacade
import com.swisscom.cf.broker.services.mongodb.enterprise.opsmanager.OpsManagerGroup
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
import static com.swisscom.cf.broker.services.mongodb.enterprise.MongoDbEnterpriseProvisionState.*
import static com.swisscom.cf.broker.util.ServiceDetailKey.*
import static com.swisscom.cf.broker.util.StringGenerator.randomAlphaNumeric

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
        Collection<ServiceDetail> details = []
        ServiceState provisionState = getProvisionState(context)
        StateMachine stateMachine = getProvisionStateMachine(context)
        def action = stateMachine.getAction(provisionState)
        def actionResult = action.triggerAction(createContext(context))

        return AsyncOperationResult.of(actionResult.success ? stateMachine.nextState(provisionState).get() : provisionState, actionResult.details)
    }

    StateMachineContext createContext(LastOperationJobContext lastOperationJobContext) {
        return null
    }

    private StateMachine getProvisionStateMachine(LastOperationJobContext context) {
        StateMachine flow = new StateMachine().withStateAndAction(INITIAL, new OnStateChange() {
            @Override
            ActionResult triggerAction(StateMachineContext stateContext) {
                OpsManagerGroup groupAndUser = opsManagerFacade.createGroup(stateContext.lastOperationJobContext.provisionRequest.serviceInstanceGuid)
                return new ActionResult(success: true, details: [
                        from(MONGODB_ENTERPRISE_GROUP_ID, groupAndUser.groupId),
                        from(MONGODB_ENTERPRISE_GROUP_NAME, groupAndUser.groupName),
                        from(MONGODB_ENTERPRISE_AGENT_API_KEY, groupAndUser.agentApiKey),
                        from(ServiceDetailKey.PORT, mongoDbEnterpriseFreePortFinder.findFreePorts(1).first().toString()),
                        from(MONGODB_ENTERPRISE_HEALTH_CHECK_USER, randomAlphaNumeric(32)),
                        from(MONGODB_ENTERPRISE_HEALTH_CHECK_PASSWORD, randomAlphaNumeric(32))])

            }
        })

        //TODO openstack processing flag per configuration
        flow.addAllFromStateMachine(BoshStateMachine.createProvisioningStateFlow(true))
        flow.withStateAndAction(BoshProvisionState.BOSH_TASK_SUCCESSFULLY_FINISHED, new OnStateChange() {
            @Override
            ActionResult triggerAction(StateMachineContext stateContext) {
                int targetAgentCount = ServiceDetailsHelper.from(stateContext.lastOperationJobContext.serviceInstance.details).getValue(MONGODB_ENTERPRISE_TARGET_AGENT_COUNT) as int
                return new ActionResult(success: opsManagerFacade.areAgentsReady(getMongoDbGroupId(context), targetAgentCount))
            }
        })
        flow.withStateAndAction(AGENTS_READY, new OnStateChange() {
            @Override
            ActionResult triggerAction(StateMachineContext stateContext) {
                String groupId = getMongoDbGroupId(context)
                int initialAutomationVersion = opsManagerFacade.getAndCheckInitialAutomationGoalVersion(groupId)
                MongoDbEnterpriseDeployment deployment = opsManagerFacade.deployReplicaSet(groupId, stateContext.lastOperationJobContext.provisionRequest.serviceInstanceGuid, ServiceDetailsHelper.from(context.serviceInstance.details).getValue(PORT) as int, ServiceDetailsHelper.from(context.serviceInstance.details).getValue(MONGODB_ENTERPRISE_HEALTH_CHECK_USER), ServiceDetailsHelper.from(context.serviceInstance.details).getValue(MONGODB_ENTERPRISE_HEALTH_CHECK_PASSWORD))

                return new ActionResult(success: true,details: [from(DATABASE, deployment.database),
                                                        from(MONGODB_ENTERPRISE_TARGET_AUTOMATION_GOAL_VERSION, String.valueOf(initialAutomationVersion + 1)),
                                                        from(MONGODB_ENTERPRISE_REPLICA_SET, deployment.replicaSet),
                                                        from(MONGODB_ENTERPRISE_MONITORING_AGENT_USER, deployment.monitoringAgentUser),
                                                        from(MONGODB_ENTERPRISE_MONITORING_AGENT_PASSWORD, deployment.monitoringAgentPassword),
                                                        from(MONGODB_ENTERPRISE_BACKUP_AGENT_USER, deployment.backupAgentUser),
                                                        from(MONGODB_ENTERPRISE_BACKUP_AGENT_PASSWORD, deployment.backupAgentPassword)])
            }
        })
        flow.withStateAndAction(AUTOMATION_UPDATE_REQUESTED, new OnStateChange() {
            @Override
            ActionResult triggerAction(StateMachineContext stateContext) {
                String groupId = getMongoDbGroupId(stateContext.lastOperationJobContext)
                int targetAutomationGoalVersion = ServiceDetailsHelper.from(stateContext.lastOperationJobContext.serviceInstance.details).getValue(MONGODB_ENTERPRISE_TARGET_AUTOMATION_GOAL_VERSION) as int
                String replicaSet = ServiceDetailsHelper.from(stateContext.lastOperationJobContext.serviceInstance.details).getValue(MONGODB_ENTERPRISE_REPLICA_SET)
                if (opsManagerFacade.isAutomationUpdateComplete(groupId, targetAutomationGoalVersion)) {
                    updateBackupConfigurationIfEnabled(groupId, replicaSet)
                    return new ActionResult(success: true)
                }else{
                    return new ActionResult(success: false)
                }
            }
        })
    }

    private void updateBackupConfigurationIfEnabled(String groupId, String replicaSetName) {
        if (serviceConfig.configureDefaultBackupOptions) {
            def success = false
            int counter = 0
            while (!success && counter++ < 5) {
                success = updateBackupConfiguration(groupId, replicaSetName)
                if (!success) {
                    try {
                        log.info("Waiting for ${serviceConfig.backupConfigRetryInMilliseconds} ms before retrying to update the backup config")
                        Thread.sleep(serviceConfig.backupConfigRetryInMilliseconds)
                    } catch (InterruptedException e) {
                        log.error(e)
                    }
                }
            }
            if (!success) {
                log.error("Backup configuration for groupId:${groupId} , replicaSet:${replicaSetName} failed")
            }
        }
    }

    private boolean updateBackupConfiguration(String groupId, String replicaSetName) {
        try {
            if (serviceConfig.opsManagerIpWhiteList) {
                opsManagerFacade.whiteListIpsForUser(serviceConfig.opsManagerUser, [serviceConfig.opsManagerIpWhiteList])
            }
            opsManagerFacade.enableBackupAndSetStorageEngine(groupId, replicaSetName)
            opsManagerFacade.updateSnapshotSchedule(groupId, replicaSetName)
            return true
        } catch (Exception e) {
            log.warn("OpsManager backup related call failed.", e)
            return false
        }
    }

    private ServiceState getProvisionState(LastOperationJobContext context) {
        ServiceState provisionState = null
        if (!context.lastOperation.internalState) {
            provisionState = MongoDbEnterpriseProvisionState.INITIAL
        } else {
            provisionState = MongoDbEnterpriseProvisionState.of(context.lastOperation.internalState)
        }
        return provisionState
    }

    @Override
    Optional<AsyncOperationResult> requestDeprovision(LastOperationJobContext context) {

    }

    private StateMachine getDeprovisionStateMachine(){
        StateMachine stateMachine = new StateMachine().withStateAndAction(MongoDbEnterpriseDeprovisionState.INITIAL, new OnStateChange() {
            @Override
            ActionResult triggerAction(StateMachineContext context) {
                String groupId = getMongoDbGroupId(context.lastOperationJobContext)
                Optional<String> optionalReplicaSet = ServiceDetailsHelper.from(context.lastOperationJobContext.serviceInstance.details).findValue(MONGODB_ENTERPRISE_REPLICA_SET)
                if (optionalReplicaSet.present) {
                    opsManagerFacade.disableAndTerminateBackup(groupId, optionalReplicaSet.get())
                } else {
                    log.warn("ReplicaSet not found for LastOperation:${context.lastOperationJobContext.lastOperation.guid}, " +
                            "the previous provisioning attempt must have failed.")
                }
                opsManagerFacade.undeploy(groupId)
                return new ActionResult(success: true)
            }
        }).withStateAndAction(MongoDbEnterpriseDeprovisionState.AUTOMATION_UPDATE_REQUESTED,new OnStateChange() {
            @Override
            ActionResult triggerAction(StateMachineContext context) {
                return new ActionResult(success:  opsManagerFacade.isAutomationUpdateComplete(getMongoDbGroupId(context.lastOperationJobContext)))

            }
        }).withStateAndAction(MongoDbEnterpriseDeprovisionState.AUTOMATION_UPDATED, new OnStateChange() {
            @Override
            ActionResult triggerAction(StateMachineContext context) {
                opsManagerFacade.deleteAllHosts(getMongoDbGroupId(context.lastOperationJobContext))
            }
        })
        //TODO openstack processing flag per configuration
        stateMachine.addAllFromStateMachine(BoshStateMachine.createDeprovisioningStateFlow(true))

        stateMachine.withStateAndAction(MongoDbEnterpriseDeprovisionState.CLEAN_UP_GROUP, new OnStateChange() {
            @Override
            ActionResult triggerAction(StateMachineContext context) {
                opsManagerFacade.deleteGroup(ServiceDetailsHelper.from(context.lastOperationJobContext.serviceInstance.details).getValue(MONGODB_ENTERPRISE_GROUP_ID))
                return new ActionResult(success: true)
            }
        })
        stateMachine.withStateAndAction(MongoDbEnterpriseDeprovisionState.DEPROVISION_SUCCESS,new NoOp())

        return stateMachine
    }

    private ServiceState getDeprovisionState(LastOperationJobContext context) {
        ServiceState deprovisionState = null
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