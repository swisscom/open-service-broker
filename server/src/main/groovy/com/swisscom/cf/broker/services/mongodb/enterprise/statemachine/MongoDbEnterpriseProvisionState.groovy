package com.swisscom.cf.broker.services.mongodb.enterprise.statemachine

import com.swisscom.cf.broker.model.LastOperation
import com.swisscom.cf.broker.provisioning.statemachine.OnStateChange
import com.swisscom.cf.broker.provisioning.statemachine.ServiceStateWithAction
import com.swisscom.cf.broker.provisioning.statemachine.StateChangeActionResult
import com.swisscom.cf.broker.provisioning.statemachine.action.NoOp
import com.swisscom.cf.broker.services.bosh.BoshProvisionState
import com.swisscom.cf.broker.provisioning.statemachine.ServiceState
import com.swisscom.cf.broker.services.mongodb.enterprise.MongoDbEnterpriseDeployment
import com.swisscom.cf.broker.services.mongodb.enterprise.MongoDbEnterpriseServiceProvider
import com.swisscom.cf.broker.services.mongodb.enterprise.opsmanager.OpsManagerGroup
import com.swisscom.cf.broker.util.ServiceDetailKey
import com.swisscom.cf.broker.util.ServiceDetailsHelper
import groovy.util.logging.Log4j

import static com.swisscom.cf.broker.model.ServiceDetail.from
import static com.swisscom.cf.broker.util.ServiceDetailKey.DATABASE
import static com.swisscom.cf.broker.util.ServiceDetailKey.MONGODB_ENTERPRISE_AGENT_API_KEY
import static com.swisscom.cf.broker.util.ServiceDetailKey.MONGODB_ENTERPRISE_BACKUP_AGENT_PASSWORD
import static com.swisscom.cf.broker.util.ServiceDetailKey.MONGODB_ENTERPRISE_BACKUP_AGENT_USER
import static com.swisscom.cf.broker.util.ServiceDetailKey.MONGODB_ENTERPRISE_GROUP_ID
import static com.swisscom.cf.broker.util.ServiceDetailKey.MONGODB_ENTERPRISE_GROUP_NAME
import static com.swisscom.cf.broker.util.ServiceDetailKey.MONGODB_ENTERPRISE_HEALTH_CHECK_PASSWORD
import static com.swisscom.cf.broker.util.ServiceDetailKey.MONGODB_ENTERPRISE_HEALTH_CHECK_USER
import static com.swisscom.cf.broker.util.ServiceDetailKey.MONGODB_ENTERPRISE_MONITORING_AGENT_PASSWORD
import static com.swisscom.cf.broker.util.ServiceDetailKey.MONGODB_ENTERPRISE_MONITORING_AGENT_USER
import static com.swisscom.cf.broker.util.ServiceDetailKey.MONGODB_ENTERPRISE_REPLICA_SET
import static com.swisscom.cf.broker.util.ServiceDetailKey.MONGODB_ENTERPRISE_TARGET_AGENT_COUNT
import static com.swisscom.cf.broker.util.ServiceDetailKey.MONGODB_ENTERPRISE_TARGET_AUTOMATION_GOAL_VERSION
import static com.swisscom.cf.broker.util.ServiceDetailKey.PORT
import static com.swisscom.cf.broker.util.StringGenerator.randomAlphaNumeric

@Log4j
enum MongoDbEnterpriseProvisionState implements ServiceStateWithAction<MongoDbEnterperiseStateMachineContext> {
    INITIAL(LastOperation.Status.IN_PROGRESS, new OnStateChange<MongoDbEnterperiseStateMachineContext>()  {
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
            return new StateChangeActionResult(go2NextState: stateContext.opsManagerFacade.areAgentsReady(MongoDbEnterpriseServiceProvider.getMongoDbGroupId(stateContext), targetAgentCount))
        }
    }),
    AGENTS_READY(LastOperation.Status.IN_PROGRESS,new OnStateChange<MongoDbEnterperiseStateMachineContext>()  {
        @Override
        StateChangeActionResult triggerAction(MongoDbEnterperiseStateMachineContext stateContext) {
            String groupId = MongoDbEnterpriseServiceProvider.getMongoDbGroupId(stateContext)
            int initialAutomationVersion = stateContext.opsManagerFacade.getAndCheckInitialAutomationGoalVersion(groupId)
            MongoDbEnterpriseDeployment deployment = stateContext.opsManagerFacade.deployReplicaSet(groupId, stateContext.lastOperationJobContext.provisionRequest.serviceInstanceGuid,
                                                                                                    ServiceDetailsHelper.from(stateContext.lastOperationJobContext.serviceInstance.details).getValue(PORT) as int,
                                                                                                    ServiceDetailsHelper.from(stateContext.lastOperationJobContext.serviceInstance.details).getValue(MONGODB_ENTERPRISE_HEALTH_CHECK_USER),
                                                                                                    ServiceDetailsHelper.from(stateContext.lastOperationJobContext.serviceInstance.details).getValue(MONGODB_ENTERPRISE_HEALTH_CHECK_PASSWORD))

            return new StateChangeActionResult(go2NextState: true,details: [from(DATABASE, deployment.database),
                                                                            from(MONGODB_ENTERPRISE_TARGET_AUTOMATION_GOAL_VERSION, String.valueOf(initialAutomationVersion + 1)),
                                                                            from(MONGODB_ENTERPRISE_REPLICA_SET, deployment.replicaSet),
                                                                            from(MONGODB_ENTERPRISE_MONITORING_AGENT_USER, deployment.monitoringAgentUser),
                                                                            from(MONGODB_ENTERPRISE_MONITORING_AGENT_PASSWORD, deployment.monitoringAgentPassword),
                                                                            from(MONGODB_ENTERPRISE_BACKUP_AGENT_USER, deployment.backupAgentUser),
                                                                            from(MONGODB_ENTERPRISE_BACKUP_AGENT_PASSWORD, deployment.backupAgentPassword)])
        }
    }),
    AUTOMATION_UPDATE_REQUESTED(LastOperation.Status.IN_PROGRESS, new OnStateChange<MongoDbEnterperiseStateMachineContext>()  {
        @Override
        StateChangeActionResult triggerAction(MongoDbEnterperiseStateMachineContext stateContext) {
            String groupId = MongoDbEnterpriseServiceProvider.getMongoDbGroupId(stateContext.lastOperationJobContext)
            int targetAutomationGoalVersion = ServiceDetailsHelper.from(stateContext.lastOperationJobContext.serviceInstance.details).getValue(MONGODB_ENTERPRISE_TARGET_AUTOMATION_GOAL_VERSION) as int
            String replicaSet = ServiceDetailsHelper.from(stateContext.lastOperationJobContext.serviceInstance.details).getValue(MONGODB_ENTERPRISE_REPLICA_SET)
            if (stateContext.opsManagerFacade.isAutomationUpdateComplete(groupId, targetAutomationGoalVersion)) {
                updateBackupConfigurationIfEnabled(stateContext, groupId, replicaSet)
                return new StateChangeActionResult(go2NextState: true)
            }else{
                return new StateChangeActionResult(go2NextState: false)
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

        private void updateBackupConfigurationIfEnabled(MongoDbEnterperiseStateMachineContext stateContext,String groupId, String replicaSetName) {
            if (stateContext.mongoDbEnterpriseConfig.configureDefaultBackupOptions) {
                def success = false
                int counter = 0
                while (!success && counter++ < 5) {
                    success = updateBackupConfiguration(groupId, replicaSetName)
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
    }),
    PROVISION_SUCCESS(LastOperation.Status.SUCCESS,new NoOp())

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