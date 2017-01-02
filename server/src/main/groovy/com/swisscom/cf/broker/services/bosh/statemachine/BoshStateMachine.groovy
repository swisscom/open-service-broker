package com.swisscom.cf.broker.services.bosh.statemachine

import com.google.common.base.Optional
import com.swisscom.cf.broker.model.ServiceDetail
import com.swisscom.cf.broker.provisioning.statemachine.StateChangeActionResult
import com.swisscom.cf.broker.provisioning.statemachine.OnStateChange
import com.swisscom.cf.broker.provisioning.statemachine.StateMachine
import com.swisscom.cf.broker.provisioning.statemachine.action.NoOp
import com.swisscom.cf.broker.services.bosh.BoshDeprovisionState
import com.swisscom.cf.broker.services.bosh.BoshProvisionState
import com.swisscom.cf.broker.util.ServiceDetailKey
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

@CompileStatic
@TypeChecked
class BoshStateMachine {

    static StateMachine createProvisioningStateFlow(boolean shouldCreateOpenStackServerGroup) {
        if (shouldCreateOpenStackServerGroup) {
            new StateMachine().withStateAndAction(BoshProvisionState.BOSH_INITIAL, new OnStateChange<BoshStateMachineContext>() {
                @Override
                StateChangeActionResult triggerAction(BoshStateMachineContext context) {
                    String serverGroupId = context.boshFacade.createOpenStackServerGroup(context.lastOperationJobContext.provisionRequest.serviceInstanceGuid)
                    new StateChangeActionResult(go2NextState: true, details:[ServiceDetail.from(ServiceDetailKey.CLOUD_PROVIDER_SERVER_GROUP_ID, serverGroupId)])
                }
            }).withStateAndAction(BoshProvisionState.CLOUD_PROVIDER_SERVER_GROUP_CREATED, new OnStateChange<BoshStateMachineContext>() {
                @Override
                StateChangeActionResult triggerAction(BoshStateMachineContext context) {
                    context.boshFacade.addOrUpdateVmInBoshCloudConfig(context.lastOperationJobContext)
                    return new StateChangeActionResult(go2NextState: true)
                }
            }).withStateAndAction(BoshProvisionState.BOSH_CLOUD_CONFIG_UPDATED, new OnStateChange<BoshStateMachineContext>() {
                @Override
                StateChangeActionResult triggerAction(BoshStateMachineContext context) {
                    new StateChangeActionResult(go2NextState: true, details: context.boshFacade.handleTemplatingAndCreateDeployment(context.lastOperationJobContext.provisionRequest, context.boshTemplateCustomizer))
                }
            }).withStateAndAction(BoshProvisionState.BOSH_DEPLOYMENT_TRIGGERED, new OnStateChange<BoshStateMachineContext>() {
                @Override
                StateChangeActionResult triggerAction(BoshStateMachineContext context) {
                    return new StateChangeActionResult(go2NextState: context.boshFacade.isBoshDeployTaskSuccessful(context.lastOperationJobContext))
                }
            })
        }else{
            new StateMachine().withStateAndAction(BoshProvisionState.BOSH_INITIAL, new OnStateChange<BoshStateMachineContext>() {
                @Override
                StateChangeActionResult triggerAction(BoshStateMachineContext context) {
                    new StateChangeActionResult(go2NextState: true, details: context.boshFacade.handleTemplatingAndCreateDeployment(context.lastOperationJobContext.provisionRequest, context.boshTemplateCustomizer))
                }
            }).withStateAndAction(BoshProvisionState.BOSH_DEPLOYMENT_TRIGGERED, new OnStateChange<BoshStateMachineContext>() {
                @Override
                StateChangeActionResult triggerAction(BoshStateMachineContext context) {
                    return new StateChangeActionResult(go2NextState: context.boshFacade.isBoshDeployTaskSuccessful(context.lastOperationJobContext))
                }
            })
        }

    }

    static StateMachine createDeprovisioningStateFlow(boolean shouldCreateOpenStackServerGroup) {
        if (shouldCreateOpenStackServerGroup) {
            new StateMachine().withStateAndAction(BoshDeprovisionState.BOSH_INITIAL, new OnStateChange<BoshStateMachineContext>() {
                @Override
                StateChangeActionResult triggerAction(BoshStateMachineContext context) {
                    Optional<String> optionalTaskId = context.boshFacade.deleteBoshDeploymentIfExists(context.lastOperationJobContext)
                    Collection<ServiceDetail> details = []
                    if(optionalTaskId.present) {
                        details.add(ServiceDetail.from(ServiceDetailKey.BOSH_TASK_ID_FOR_UNDEPLOY, optionalTaskId.get()))
                    }
                    return new StateChangeActionResult(go2NextState: true,details: details)
                }
            }).withStateAndAction(BoshDeprovisionState.BOSH_DEPLOYMENT_DELETION_REQUESTED, new OnStateChange<BoshStateMachineContext>() {
                @Override
                StateChangeActionResult triggerAction(BoshStateMachineContext context) {
                    return new StateChangeActionResult(go2NextState: context.boshFacade.isBoshUndeployTaskSuccessful(context.lastOperationJobContext))
                }
            }).withStateAndAction(BoshDeprovisionState.BOSH_TASK_SUCCESSFULLY_FINISHED, new OnStateChange<BoshStateMachineContext>() {
                @Override
                StateChangeActionResult triggerAction(BoshStateMachineContext context) {
                    context.boshFacade.removeVmInBoshCloudConfig(context.lastOperationJobContext)
                    return new StateChangeActionResult(go2NextState: true)
                }
            }).withStateAndAction(BoshDeprovisionState.BOSH_CLOUD_CONFIG_UPDATED, new OnStateChange<BoshStateMachineContext>() {
                @Override
                StateChangeActionResult triggerAction(BoshStateMachineContext context) {
                    context.boshFacade.deleteOpenStackServerGroupIfExists(context.lastOperationJobContext)
                    return new StateChangeActionResult(go2NextState: true)
               }
            }).withStateAndAction(BoshDeprovisionState.BOSH_FINAL, new NoOp())
        }else{
            new StateMachine().withStateAndAction(BoshDeprovisionState.BOSH_INITIAL, new OnStateChange<BoshStateMachineContext>() {
                @Override
                StateChangeActionResult triggerAction(BoshStateMachineContext context) {
                    Optional<String> optionalTaskId = context.boshFacade.deleteBoshDeploymentIfExists(context.lastOperationJobContext)
                    Collection<ServiceDetail> details = []
                    if(optionalTaskId.present) {
                        details.add(ServiceDetail.from(ServiceDetailKey.BOSH_TASK_ID_FOR_UNDEPLOY, optionalTaskId.get()))
                    }
                    return new StateChangeActionResult(go2NextState: true,details: details)
                }
            }).withStateAndAction(BoshDeprovisionState.BOSH_DEPLOYMENT_DELETION_REQUESTED, new OnStateChange<BoshStateMachineContext>() {
                @Override
                StateChangeActionResult triggerAction(BoshStateMachineContext context) {
                    return new StateChangeActionResult(go2NextState: context.boshFacade.isBoshUndeployTaskSuccessful(context.lastOperationJobContext))
                }
            }).withStateAndAction(BoshDeprovisionState.BOSH_FINAL, new NoOp())
        }
    }

}
