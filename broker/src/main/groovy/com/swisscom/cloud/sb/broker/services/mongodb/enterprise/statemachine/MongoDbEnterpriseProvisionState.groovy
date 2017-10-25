package com.swisscom.cloud.sb.broker.services.mongodb.enterprise.statemachine

import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.provisioning.statemachine.OnStateChange
import com.swisscom.cloud.sb.broker.provisioning.statemachine.ServiceState
import com.swisscom.cloud.sb.broker.provisioning.statemachine.ServiceStateWithAction
import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateChangeActionResult
import com.swisscom.cloud.sb.broker.provisioning.statemachine.action.NoOp
import com.swisscom.cloud.sb.broker.services.bosh.statemachine.BoshProvisionState
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.MongoDbEnterpriseDeployment
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.MongoDbEnterpriseServiceProvider
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.opsmanager.OpsManagerGroup
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailKey
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper
import groovy.util.logging.Slf4j

import static com.swisscom.cloud.sb.broker.model.ServiceDetail.from
import static com.swisscom.cloud.sb.broker.services.mongodb.enterprise.MongoDbEnterpriseServiceDetailKey.*
import static com.swisscom.cloud.sb.broker.util.StringGenerator.randomAlphaNumeric

@Slf4j
enum MongoDbEnterpriseProvisionState implements ServiceStateWithAction<MongoDbEnterperiseStateMachineContext> {


    CREATE_OPS_MANAGER_GROUP(LastOperation.Status.IN_PROGRESS, new OnStateChange<MongoDbEnterperiseStateMachineContext>()  {
        @Override
        StateChangeActionResult triggerAction(MongoDbEnterperiseStateMachineContext stateContext) {
            OpsManagerGroup groupAndUser = stateContext.opsManagerFacade.createGroup(stateContext.lastOperationJobContext.provisionRequest.serviceInstanceGuid)
            return new StateChangeActionResult(go2NextState: true, details: [
                    from(MONGODB_ENTERPRISE_GROUP_ID, groupAndUser.groupId),
                    from(MONGODB_ENTERPRISE_GROUP_NAME, groupAndUser.groupName),
                    from(MONGODB_ENTERPRISE_AGENT_API_KEY, groupAndUser.agentApiKey),
                    from(ServiceDetailKey.PORT, stateContext.mongoDbEnterpriseFreePortFinder.findFreePorts(1).first().toString()),
                    from(MONGODB_ENTERPRISE_HEALTH_CHECK_USER, randomAlphaNumeric(32)),
                    from(MONGODB_ENTERPRISE_HEALTH_CHECK_PASSWORD, randomAlphaNumeric(32))])

        }
    }),
    CHECK_AGENTS(LastOperation.Status.IN_PROGRESS,new OnStateChange<MongoDbEnterperiseStateMachineContext>()  {
        @Override
        StateChangeActionResult triggerAction(MongoDbEnterperiseStateMachineContext stateContext) {
            int targetAgentCount = ServiceDetailsHelper.from(stateContext.lastOperationJobContext.serviceInstance.details).getValue(MONGODB_ENTERPRISE_TARGET_AGENT_COUNT) as int
            return new StateChangeActionResult(go2NextState: stateContext.opsManagerFacade.areAgentsReady(MongoDbEnterpriseServiceProvider.getMongoDbGroupId(stateContext.lastOperationJobContext), targetAgentCount))
        }
    }),
    REQUEST_AUTOMATION_UPDATE(LastOperation.Status.IN_PROGRESS,new OnStateChange<MongoDbEnterperiseStateMachineContext>()  {

        @Override
        StateChangeActionResult triggerAction(MongoDbEnterperiseStateMachineContext stateContext) {
            String groupId = MongoDbEnterpriseServiceProvider.getMongoDbGroupId(stateContext.lastOperationJobContext)
            int initialAutomationVersion = stateContext.opsManagerFacade.getAndCheckInitialAutomationGoalVersion(groupId)
            MongoDbEnterpriseDeployment deployment = stateContext.opsManagerFacade.deployReplicaSet(groupId, stateContext.lastOperationJobContext.provisionRequest.serviceInstanceGuid,
                                                                                                    ServiceDetailsHelper.from(stateContext.lastOperationJobContext.serviceInstance.details).getValue(ServiceDetailKey.PORT) as int,
                                                                                                    ServiceDetailsHelper.from(stateContext.lastOperationJobContext.serviceInstance.details).getValue(MONGODB_ENTERPRISE_HEALTH_CHECK_USER),
                    ServiceDetailsHelper.from(stateContext.lastOperationJobContext.serviceInstance.details).getValue(MONGODB_ENTERPRISE_HEALTH_CHECK_PASSWORD),
                    findTargetMongoDBVersion(stateContext))

            return new StateChangeActionResult(go2NextState: true,details: [from(ServiceDetailKey.DATABASE, deployment.database),
                                                                            from(MONGODB_ENTERPRISE_TARGET_AUTOMATION_GOAL_VERSION, String.valueOf(initialAutomationVersion + 1)),
                                                                            from(MONGODB_ENTERPRISE_REPLICA_SET, deployment.replicaSet),
                                                                            from(MONGODB_ENTERPRISE_MONITORING_AGENT_USER, deployment.monitoringAgentUser),
                                                                            from(MONGODB_ENTERPRISE_MONITORING_AGENT_PASSWORD, deployment.monitoringAgentPassword),
                                                                            from(MONGODB_ENTERPRISE_BACKUP_AGENT_USER, deployment.backupAgentUser),
                                                                            from(MONGODB_ENTERPRISE_BACKUP_AGENT_PASSWORD, deployment.backupAgentPassword)])
        }

        static String findTargetMongoDBVersion(MongoDbEnterperiseStateMachineContext context) {
            def mongoDbVersion = context.lastOperationJobContext.plan.parameters?.find({
                it.name == MongoDbEnterpriseProvisionState.PLAN_PARAMETER_MONGODB_VERSION
            })?.value
            log.info("Could not find a '${PLAN_PARAMETER_MONGODB_VERSION}' parameter in plan. Falling back to SB wide configuration.")
            return mongoDbVersion ?: context.mongoDbEnterpriseConfig.mongoDbVersion
        }

    }),
    CHECK_AUTOMATION_UPDATE_STATUS(LastOperation.Status.IN_PROGRESS, new OnStateChange<MongoDbEnterperiseStateMachineContext>()  {
        @Override
        StateChangeActionResult triggerAction(MongoDbEnterperiseStateMachineContext stateContext) {
            String groupId = MongoDbEnterpriseServiceProvider.getMongoDbGroupId(stateContext.lastOperationJobContext)
            int targetAutomationGoalVersion = ServiceDetailsHelper.from(stateContext.lastOperationJobContext.serviceInstance.details).getValue(MONGODB_ENTERPRISE_TARGET_AUTOMATION_GOAL_VERSION) as int
            return new StateChangeActionResult(go2NextState: stateContext.opsManagerFacade.isAutomationUpdateComplete(groupId, targetAutomationGoalVersion))
        }

    }),
    ENABLE_BACKUP_IF_CONFIGURED(LastOperation.Status.IN_PROGRESS, new OnStateChange<MongoDbEnterperiseStateMachineContext>()  {
        @Override
        StateChangeActionResult triggerAction(MongoDbEnterperiseStateMachineContext stateContext) {
            String groupId = MongoDbEnterpriseServiceProvider.getMongoDbGroupId(stateContext.lastOperationJobContext)
            String replicaSet = ServiceDetailsHelper.from(stateContext.lastOperationJobContext.serviceInstance.details).getValue(MONGODB_ENTERPRISE_REPLICA_SET)
            updateBackupConfigurationIfEnabled(stateContext,groupId,replicaSet)
            return new StateChangeActionResult(go2NextState: true)

        }
        private void updateBackupConfigurationIfEnabled(MongoDbEnterperiseStateMachineContext stateContext,String groupId, String replicaSetName) {
            if (stateContext.mongoDbEnterpriseConfig.configureDefaultBackupOptions) {
                def success = false
                int counter = 0
                while (!success && counter++ < 5) {
                    success = updateBackupConfiguration(stateContext, groupId, replicaSetName)
                    if (!success) {
                        try {
                            log.info("Waiting for ${stateContext.mongoDbEnterpriseConfig.backupConfigRetryInMilliseconds} ms before retrying to update the backup config")
                            Thread.sleep(stateContext.mongoDbEnterpriseConfig.backupConfigRetryInMilliseconds)
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

        private boolean updateBackupConfiguration(MongoDbEnterperiseStateMachineContext stateContext,String groupId, String replicaSetName) {
            try {
                if (stateContext.mongoDbEnterpriseConfig.opsManagerIpWhiteList) {
                    stateContext.opsManagerFacade.whiteListIpsForUser(stateContext.mongoDbEnterpriseConfig.opsManagerUser, [stateContext.mongoDbEnterpriseConfig.opsManagerIpWhiteList])
                }
                stateContext.opsManagerFacade.enableBackupAndSetStorageEngine(groupId, replicaSetName)
                stateContext.opsManagerFacade.updateSnapshotSchedule(groupId, replicaSetName)
                return true
            } catch (Exception e) {
                log.warn("OpsManager backup related call failed.", e)
                return false
            }
        }


    }),
    PROVISION_SUCCESS(LastOperation.Status.SUCCESS,new NoOp())


    public static final String PLAN_PARAMETER_MONGODB_VERSION = "MONGODB_VERSION"
    public static final Map<String, ServiceState> map = new TreeMap<String, ServiceState>()

    static {
        for (ServiceState serviceState : values() + BoshProvisionState.values()) {
            if (map.containsKey(serviceState.getServiceInternalState())) {
                throw new RuntimeException("Enum:${serviceState.getServiceInternalState()} already exists in:${MongoDbEnterpriseProvisionState.class.getSimpleName()}!")
            } else {
                map.put(serviceState.getServiceInternalState(), serviceState);
            }
        }
    }

    private final LastOperation.Status status
    private final OnStateChange<MongoDbEnterperiseStateMachineContext> onStateChange

    MongoDbEnterpriseProvisionState(LastOperation.Status lastOperationStatus,OnStateChange<MongoDbEnterperiseStateMachineContext> onStateChange) {
        this.status = lastOperationStatus
        this.onStateChange = onStateChange
    }

    @Override
    LastOperation.Status getLastOperationStatus() {
        return status
    }

    @Override
    String getServiceInternalState() {
        return name()
    }

    public static ServiceStateWithAction of(String state) {
        return map.get(state)
    }

    @Override
    StateChangeActionResult triggerAction(MongoDbEnterperiseStateMachineContext context) {
        return onStateChange.triggerAction(context)
    }
}