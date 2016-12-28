package com.swisscom.cf.broker.services.bosh

import com.swisscom.cf.broker.model.ServiceDetail
import com.swisscom.cf.broker.provisioning.statemachine.ActionResult
import com.swisscom.cf.broker.provisioning.statemachine.OnStateChange
import com.swisscom.cf.broker.provisioning.statemachine.StateMachineContext
import com.swisscom.cf.broker.provisioning.statemachine.StateMachine
import com.swisscom.cf.broker.util.ServiceDetailKey
import groovy.transform.CompileStatic


@CompileStatic
class BoshStateMachine {

    static StateMachine createProvisioningStateFlow(boolean shouldCreateOpenStackServerGroup) {


        if (shouldCreateOpenStackServerGroup) {
            new StateMachine().withStateAndAction(BoshProvisionState.BOSH_INITIAL, new OnStateChange() {
                @Override
                ActionResult triggerAction(StateMachineContext context) {
                    String serverGroupId = context.boshFacade.createOpenStackServerGroup(context.lastOperationJobContext.provisionRequest.serviceInstanceGuid)
                    new ActionResult(success: true, details:[ ServiceDetail.from(ServiceDetailKey.CLOUD_PROVIDER_SERVER_GROUP_ID, serverGroupId)])
                }
            }).withStateAndAction(BoshProvisionState.CLOUD_PROVIDER_SERVER_GROUP_CREATED, new OnStateChange() {
                @Override
                ActionResult triggerAction(StateMachineContext context) {
                    context.boshFacade.addOrUpdateVmInBoshCloudConfig(context.lastOperationJobContext)
                    return new ActionResult(success: true)
                }
            }).withStateAndAction(BoshProvisionState.BOSH_CLOUD_CONFIG_UPDATED, new OnStateChange() {
                @Override
                ActionResult triggerAction(StateMachineContext context) {
                    new ActionResult(success: true, details: context.boshFacade.handleTemplatingAndCreateDeployment(context.lastOperationJobContext.provisionRequest, context.boshTemplateCustomizer))
                }
            }).withStateAndAction(BoshProvisionState.BOSH_DEPLOYMENT_TRIGGERED, new OnStateChange() {
                @Override
                ActionResult triggerAction(StateMachineContext context) {
                    return new ActionResult(success: context.boshFacade.isBoshTaskSuccessful(context.lastOperationJobContext))
                }
            })
        }else{
            new StateMachine().withStateAndAction(BoshProvisionState.BOSH_INITIAL, new OnStateChange() {
                @Override
                ActionResult triggerAction(StateMachineContext context) {
                    new ActionResult(success: true, details: context.boshFacade.handleTemplatingAndCreateDeployment(context.lastOperationJobContext.provisionRequest, context.boshTemplateCustomizer))
                }
            }).withStateAndAction(BoshProvisionState.BOSH_DEPLOYMENT_TRIGGERED, new OnStateChange() {
                @Override
                ActionResult triggerAction(StateMachineContext context) {
                    return new ActionResult(success: context.boshFacade.isBoshTaskSuccessful(context.lastOperationJobContext))
                }
            })
        }

    }
}
