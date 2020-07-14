/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.broker.services.bosh.statemachine

import com.google.common.base.Optional
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.RequestWithParameters
import com.swisscom.cloud.sb.broker.provisioning.statemachine.OnStateChange
import com.swisscom.cloud.sb.broker.provisioning.statemachine.ServiceStateWithAction
import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateChangeActionResult
import groovy.transform.CompileStatic

@CompileStatic
enum BoshUpdateState implements ServiceStateWithAction<BoshStateMachineContext> {
    UPDATE_CONFIGS(LastOperation.Status.IN_PROGRESS, new OnStateChange<BoshStateMachineContext>() {
        @Override
        StateChangeActionResult triggerAction(BoshStateMachineContext context) {
            context.boshFacade.handleTemplatingAndCreateConfigs(context.lastOperationJobContext.updateRequest.serviceInstanceGuid,
                                                                context.boshTemplateCustomizer)
            new StateChangeActionResult(go2NextState: true)
        }
    }),
    UPDATE_DEPLOYMENT(LastOperation.Status.IN_PROGRESS, new OnStateChange<BoshStateMachineContext>() {
        @Override
        StateChangeActionResult triggerAction(BoshStateMachineContext context) {
            RequestWithParameters request = context.lastOperationJobContext.updateRequest
            new StateChangeActionResult(go2NextState: true,
                                        details: context.boshFacade.handleTemplatingAndCreateDeployment(request.serviceInstanceGuid,
                                                                                                        request.plan.templateUniqueIdentifier,
                                                                                                        request.plan.parameters.toSet(),
                                                                                                        context.boshTemplateCustomizer))
        }
    }),
    CHECK_BOSH_DEPLOYMENT_TASK_STATE(LastOperation.Status.IN_PROGRESS, new OnStateChange<BoshStateMachineContext>() {
        @Override
        StateChangeActionResult triggerAction(BoshStateMachineContext context) {
            return new StateChangeActionResult(go2NextState: context.boshFacade.isBoshDeployTaskSuccessful(context.lastOperationJobContext.serviceInstance.details))
        }
    })

    final LastOperation.Status status
    final OnStateChange<BoshStateMachineContext> onStateChange

    BoshUpdateState(final LastOperation.Status status, OnStateChange<BoshStateMachineContext> onStateChange) {
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

    static Optional<BoshUpdateState> of(String text) {
        def result = BoshUpdateState.values().find {it.name() == text}
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
