package com.swisscom.cf.broker.services.bosh

import com.google.common.base.Optional
import com.swisscom.cf.broker.model.LastOperation
import com.swisscom.cf.broker.model.ServiceDetail
import com.swisscom.cf.broker.provisioning.statemachine.OnStateChange
import com.swisscom.cf.broker.provisioning.statemachine.ServiceStateWithAction
import com.swisscom.cf.broker.provisioning.statemachine.StateChangeActionResult
import com.swisscom.cf.broker.provisioning.statemachine.action.NoOp
import com.swisscom.cf.broker.services.bosh.statemachine.BoshStateMachineContext
import com.swisscom.cf.broker.util.ServiceDetailKey
import groovy.transform.CompileStatic

@CompileStatic
enum BoshProvisionState implements ServiceStateWithAction<BoshStateMachineContext> {
    BOSH_INITIAL(LastOperation.Status.IN_PROGRESS, new OnStateChange<BoshStateMachineContext>() {
        @Override
        StateChangeActionResult triggerAction(BoshStateMachineContext context) {
            String serverGroupId = context.boshFacade.createOpenStackServerGroup(context.lastOperationJobContext.provisionRequest.serviceInstanceGuid)
            new StateChangeActionResult(go2NextState: true, details:[ServiceDetail.from(ServiceDetailKey.CLOUD_PROVIDER_SERVER_GROUP_ID, serverGroupId)])
        }
    }),
    CLOUD_PROVIDER_SERVER_GROUP_CREATED(LastOperation.Status.IN_PROGRESS,new OnStateChange<BoshStateMachineContext>() {
        @Override
        StateChangeActionResult triggerAction(BoshStateMachineContext context) {
            context.boshFacade.addOrUpdateVmInBoshCloudConfig(context.lastOperationJobContext)
            return new StateChangeActionResult(go2NextState: true)
        }
    }),
    BOSH_CLOUD_CONFIG_UPDATED(LastOperation.Status.IN_PROGRESS, new OnStateChange<BoshStateMachineContext>() {
        @Override
        StateChangeActionResult triggerAction(BoshStateMachineContext context) {
            new StateChangeActionResult(go2NextState: true, details: context.boshFacade.handleTemplatingAndCreateDeployment(context.lastOperationJobContext.provisionRequest, context.boshTemplateCustomizer))
        }
    }),
    BOSH_DEPLOYMENT_TRIGGERED(LastOperation.Status.IN_PROGRESS, new OnStateChange<BoshStateMachineContext>() {
        @Override
        StateChangeActionResult triggerAction(BoshStateMachineContext context) {
            return new StateChangeActionResult(go2NextState: context.boshFacade.isBoshDeployTaskSuccessful(context.lastOperationJobContext))
        }
    }),
    BOSH_TASK_SUCCESSFULLY_FINISHED(LastOperation.Status.IN_PROGRESS,new NoOp())

    final LastOperation.Status status
    final OnStateChange<BoshStateMachineContext> onStateChange

    BoshProvisionState(final LastOperation.Status status,OnStateChange<BoshStateMachineContext> onStateChange) {
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

    static Optional<BoshProvisionState> of(String text) {
        def result = BoshProvisionState.values().find { it.name() == text }
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
