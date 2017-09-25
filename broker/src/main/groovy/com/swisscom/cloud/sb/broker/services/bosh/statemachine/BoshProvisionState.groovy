package com.swisscom.cloud.sb.broker.services.bosh.statemachine

import com.google.common.base.Optional
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.provisioning.statemachine.OnStateChange
import com.swisscom.cloud.sb.broker.provisioning.statemachine.ServiceStateWithAction
import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateChangeActionResult
import com.swisscom.cloud.sb.broker.services.bosh.BoshServiceDetailKey
import groovy.transform.CompileStatic

@CompileStatic
enum BoshProvisionState implements ServiceStateWithAction<BoshStateMachineContext> {
    CREATE_OPEN_STACK_SERVER_GROUP(LastOperation.Status.IN_PROGRESS, new OnStateChange<BoshStateMachineContext>() {
        @Override
        StateChangeActionResult triggerAction(BoshStateMachineContext context) {
            String serverGroupId = context.boshFacade.createOpenStackServerGroup(context.lastOperationJobContext.provisionRequest.serviceInstanceGuid)
            new StateChangeActionResult(go2NextState: true, details:[ServiceDetail.from(BoshServiceDetailKey.CLOUD_PROVIDER_SERVER_GROUP_ID, serverGroupId)])
        }
    }),
    UPDATE_BOSH_CLOUD_CONFIG(LastOperation.Status.IN_PROGRESS,new OnStateChange<BoshStateMachineContext>() {
        @Override
        StateChangeActionResult triggerAction(BoshStateMachineContext context) {
            context.boshFacade.addOrUpdateVmInBoshCloudConfig(context.lastOperationJobContext)
            return new StateChangeActionResult(go2NextState: true)
        }
    }),
    CREATE_DEPLOYMENT(LastOperation.Status.IN_PROGRESS, new OnStateChange<BoshStateMachineContext>() {
        @Override
        StateChangeActionResult triggerAction(BoshStateMachineContext context) {
            new StateChangeActionResult(go2NextState: true, details: context.boshFacade.handleTemplatingAndCreateDeployment(context.lastOperationJobContext.provisionRequest, context.boshTemplateCustomizer))
        }
    }),
    CHECK_BOSH_DEPLOYMENT_TASK_STATE(LastOperation.Status.IN_PROGRESS, new OnStateChange<BoshStateMachineContext>() {
        @Override
        StateChangeActionResult triggerAction(BoshStateMachineContext context) {
            return new StateChangeActionResult(go2NextState: context.boshFacade.isBoshDeployTaskSuccessful(context.lastOperationJobContext))
        }
    })

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
