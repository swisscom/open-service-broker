package com.swisscom.cf.broker.services.mongodb.enterprise.statemachine

import com.google.common.base.Optional
import com.swisscom.cf.broker.model.LastOperation
import com.swisscom.cf.broker.provisioning.statemachine.OnStateChange
import com.swisscom.cf.broker.provisioning.statemachine.ServiceState
import com.swisscom.cf.broker.provisioning.statemachine.ServiceStateWithAction
import com.swisscom.cf.broker.provisioning.statemachine.StateChangeActionResult
import com.swisscom.cf.broker.provisioning.statemachine.action.NoOp
import com.swisscom.cf.broker.services.bosh.BoshDeprovisionState
import com.swisscom.cf.broker.services.mongodb.enterprise.MongoDbEnterpriseServiceProvider
import com.swisscom.cf.broker.util.ServiceDetailsHelper
import groovy.util.logging.Log4j

import static com.swisscom.cf.broker.util.ServiceDetailKey.MONGODB_ENTERPRISE_GROUP_ID
import static com.swisscom.cf.broker.util.ServiceDetailKey.MONGODB_ENTERPRISE_REPLICA_SET

@Log4j
enum MongoDbEnterpriseDeprovisionState implements ServiceStateWithAction<MongoDbEnterperiseStateMachineContext> {
    INITIAL(LastOperation.Status.IN_PROGRESS,new OnStateChange<MongoDbEnterperiseStateMachineContext>() {
        @Override
        StateChangeActionResult triggerAction(MongoDbEnterperiseStateMachineContext context) {
            String groupId = MongoDbEnterpriseServiceProvider.getMongoDbGroupId(context.lastOperationJobContext)
            Optional<String> optionalReplicaSet = ServiceDetailsHelper.from(context.lastOperationJobContext.serviceInstance.details).findValue(MONGODB_ENTERPRISE_REPLICA_SET)
            if (optionalReplicaSet.present) {
                context.opsManagerFacade.disableAndTerminateBackup(groupId, optionalReplicaSet.get())
            } else {
                log.warn("ReplicaSet not found for LastOperation:${context.lastOperationJobContext.lastOperation.guid}, " +
                        "the previous provisioning attempt must have failed.")
            }
            context.opsManagerFacade.undeploy(groupId)
            return new StateChangeActionResult(go2NextState: true)
        }
    }),
    AUTOMATION_UPDATE_REQUESTED(LastOperation.Status.IN_PROGRESS,new OnStateChange<MongoDbEnterperiseStateMachineContext>()  {
        @Override
        StateChangeActionResult triggerAction(MongoDbEnterperiseStateMachineContext context) {
            return new StateChangeActionResult(go2NextState:  context.opsManagerFacade.isAutomationUpdateComplete(MongoDbEnterpriseServiceProvider.getMongoDbGroupId(context.lastOperationJobContext)))

        }
    }),
    AUTOMATION_UPDATED(LastOperation.Status.IN_PROGRESS,new OnStateChange<MongoDbEnterperiseStateMachineContext>()  {
        @Override
        StateChangeActionResult triggerAction(MongoDbEnterperiseStateMachineContext context) {
            context.opsManagerFacade.deleteAllHosts(MongoDbEnterpriseServiceProvider.getMongoDbGroupId(context.lastOperationJobContext))
        }
    }),
    CLEAN_UP_GROUP(LastOperation.Status.IN_PROGRESS,new OnStateChange<MongoDbEnterperiseStateMachineContext>()  {
        @Override
        StateChangeActionResult triggerAction(MongoDbEnterperiseStateMachineContext context) {
            context.opsManagerFacade.deleteGroup(ServiceDetailsHelper.from(context.lastOperationJobContext.serviceInstance.details).getValue(MONGODB_ENTERPRISE_GROUP_ID))
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