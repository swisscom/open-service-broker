package com.swisscom.cloud.sb.broker.services.mongodb.enterprise.statemachine

import com.google.common.base.Optional
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.provisioning.statemachine.OnStateChange
import com.swisscom.cloud.sb.broker.provisioning.statemachine.ServiceState
import com.swisscom.cloud.sb.broker.provisioning.statemachine.ServiceStateWithAction
import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateChangeActionResult
import com.swisscom.cloud.sb.broker.provisioning.statemachine.action.NoOp
import com.swisscom.cloud.sb.broker.services.bosh.statemachine.BoshDeprovisionState
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.MongoDbEnterpriseServiceDetailKey
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.MongoDbEnterpriseServiceProvider
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper
import groovy.util.logging.Slf4j

@Slf4j
enum MongoDbEnterpriseDeprovisionState implements ServiceStateWithAction<MongoDbEnterperiseStateMachineContext> {
    DISABLE_BACKUP_IF_ENABLED(LastOperation.Status.IN_PROGRESS,new OnStateChange<MongoDbEnterperiseStateMachineContext>() {
        @Override
        StateChangeActionResult triggerAction(MongoDbEnterperiseStateMachineContext context) {
            String groupId = MongoDbEnterpriseServiceProvider.getMongoDbGroupId(context.lastOperationJobContext)
            Optional<String> optionalReplicaSet = ServiceDetailsHelper.from(context.lastOperationJobContext.serviceInstance.details).findValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_REPLICA_SET)
            if (optionalReplicaSet.present) {
                context.opsManagerFacade.disableAndTerminateBackup(groupId, optionalReplicaSet.get())
            } else {
                log.warn("ReplicaSet not found for LastOperation:${context.lastOperationJobContext.lastOperation.guid}, " +
                        "the previous provisioning attempt must have failed.")
            }
            return new StateChangeActionResult(go2NextState: true)
        }
    }),
    UPDATE_AUTOMATION_CONFIG(LastOperation.Status.IN_PROGRESS,new OnStateChange<MongoDbEnterperiseStateMachineContext>() {
        @Override
        StateChangeActionResult triggerAction(MongoDbEnterperiseStateMachineContext context) {
            context.opsManagerFacade.undeploy(MongoDbEnterpriseServiceProvider.getMongoDbGroupId(context.lastOperationJobContext))
            return new StateChangeActionResult(go2NextState: true)
        }
    }),
    CHECK_AUTOMATION_CONFIG_STATE(LastOperation.Status.IN_PROGRESS,new OnStateChange<MongoDbEnterperiseStateMachineContext>()  {
        @Override
        StateChangeActionResult triggerAction(MongoDbEnterperiseStateMachineContext context) {
            return new StateChangeActionResult(go2NextState:  context.opsManagerFacade.isAutomationUpdateComplete(MongoDbEnterpriseServiceProvider.getMongoDbGroupId(context.lastOperationJobContext)))

        }
    }),
    DELETE_HOSTS_ON_OPS_MANAGER(LastOperation.Status.IN_PROGRESS,new OnStateChange<MongoDbEnterperiseStateMachineContext>()  {
        @Override
        StateChangeActionResult triggerAction(MongoDbEnterperiseStateMachineContext context) {
            context.opsManagerFacade.deleteAllHosts(MongoDbEnterpriseServiceProvider.getMongoDbGroupId(context.lastOperationJobContext))
            return new StateChangeActionResult(go2NextState: true)
        }
    }),
    CLEAN_UP_GROUP(LastOperation.Status.IN_PROGRESS,new OnStateChange<MongoDbEnterperiseStateMachineContext>()  {
        @Override
        StateChangeActionResult triggerAction(MongoDbEnterperiseStateMachineContext context) {
            context.opsManagerFacade.deleteGroup(ServiceDetailsHelper.from(context.lastOperationJobContext.serviceInstance.details).getValue(MongoDbEnterpriseServiceDetailKey.MONGODB_ENTERPRISE_GROUP_ID))
            return new StateChangeActionResult(go2NextState: true)
        }
    }),
    DEPROVISION_SUCCESS(LastOperation.Status.SUCCESS, new NoOp())

    public static final Map<String, ServiceState> map = new TreeMap<String, ServiceState>()

    static {
        for (ServiceState serviceState : values() + BoshDeprovisionState.values()) {
            if (map.containsKey(serviceState.getServiceInternalState())) {
                throw new RuntimeException("Enum:${serviceState.getServiceInternalState()} already exists in:${MongoDbEnterpriseDeprovisionState.class.getSimpleName()}!")
            } else {
                map.put(serviceState.getServiceInternalState(), serviceState);
            }
        }
    }
    private final LastOperation.Status status
    private final OnStateChange<MongoDbEnterperiseStateMachineContext> onStateChange

    MongoDbEnterpriseDeprovisionState(LastOperation.Status lastOperationStatus,OnStateChange<MongoDbEnterperiseStateMachineContext> onStateChange) {
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
        return map.get(state);
    }

    @Override
    StateChangeActionResult triggerAction(MongoDbEnterperiseStateMachineContext context) {
        return onStateChange.triggerAction(context)
    }
}