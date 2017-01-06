package com.swisscom.cf.broker.services.bosh.statemachine

import com.google.common.base.Optional
import com.swisscom.cf.broker.model.LastOperation
import com.swisscom.cf.broker.model.ServiceDetail
import com.swisscom.cf.broker.provisioning.statemachine.OnStateChange
import com.swisscom.cf.broker.provisioning.statemachine.ServiceStateWithAction
import com.swisscom.cf.broker.provisioning.statemachine.StateChangeActionResult
import com.swisscom.cf.broker.provisioning.statemachine.action.NoOp
import com.swisscom.cf.broker.util.ServiceDetailKey
import groovy.transform.CompileStatic

@CompileStatic
enum BoshDeprovisionState implements ServiceStateWithAction<BoshStateMachineContext> {
    BOSH_INITIAL(LastOperation.Status.IN_PROGRESS,new OnStateChange<BoshStateMachineContext>() {
        @Override
        StateChangeActionResult triggerAction(BoshStateMachineContext context) {
            Optional<String> optionalTaskId = context.boshFacade.deleteBoshDeploymentIfExists(context.lastOperationJobContext)
            Collection<ServiceDetail> details = []
            if(optionalTaskId.present) {
                details.add(ServiceDetail.from(ServiceDetailKey.BOSH_TASK_ID_FOR_UNDEPLOY, optionalTaskId.get()))
            }
            return new StateChangeActionResult(go2NextState: true,details: details)
        }
    }),
    BOSH_DEPLOYMENT_DELETION_REQUESTED(LastOperation.Status.IN_PROGRESS, new OnStateChange<BoshStateMachineContext>() {
        @Override
        StateChangeActionResult triggerAction(BoshStateMachineContext context) {
            return new StateChangeActionResult(go2NextState: context.boshFacade.isBoshUndeployTaskSuccessful(context.lastOperationJobContext))
        }
    }),
    BOSH_TASK_SUCCESSFULLY_FINISHED(LastOperation.Status.IN_PROGRESS, new OnStateChange<BoshStateMachineContext>() {
        @Override
        StateChangeActionResult triggerAction(BoshStateMachineContext context) {
            context.boshFacade.removeVmInBoshCloudConfig(context.lastOperationJobContext)
            return new StateChangeActionResult(go2NextState: true)
        }
    }),
    BOSH_CLOUD_CONFIG_UPDATED(LastOperation.Status.IN_PROGRESS, new OnStateChange<BoshStateMachineContext>() {
        @Override
        StateChangeActionResult triggerAction(BoshStateMachineContext context) {
            context.boshFacade.deleteOpenStackServerGroupIfExists(context.lastOperationJobContext)
            return new StateChangeActionResult(go2NextState: true)
        }
    }),
    BOSH_FINAL(LastOperation.Status.IN_PROGRESS,new NoOp())

    final LastOperation.Status status
    final OnStateChange<BoshStateMachineContext> onStateChange

    BoshDeprovisionState(final LastOperation.Status status,OnStateChange<BoshStateMachineContext> onStateChange) {
        this.status = status
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

    static Optional<BoshDeprovisionState> of(String text) {
        def result = BoshDeprovisionState.values().find { it.name() == text }
        if (!result) {
            return Optional.absent()
        }
        return Optional.of(result)
    }

    @Override
    StateChangeActionResult triggerAction(BoshStateMachineContext context) {
        return onStateChange.triggerAction(context)
    }
}
