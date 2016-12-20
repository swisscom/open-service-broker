package com.swisscom.cf.broker.services.bosh

import com.swisscom.cf.broker.model.ServiceDetail
import com.swisscom.cf.broker.provisioning.state.ActionResult
import com.swisscom.cf.broker.provisioning.state.OnStateChange
import com.swisscom.cf.broker.provisioning.state.StateContext
import com.swisscom.cf.broker.provisioning.state.StateFlow
import com.swisscom.cf.broker.util.ServiceDetailKey
import groovy.transform.CompileStatic


@CompileStatic
class BoshStateFlow {

    static StateFlow createProvisioningStateFlow(boolean shouldCreateOpenStackServerGroup) {


        if (shouldCreateOpenStackServerGroup) {
            new StateFlow().withStateAndAction(BoshProvisionState.BOSH_INITIAL, new OnStateChange() {
                @Override
                ActionResult triggerAction(StateContext context) {
                    String serverGroupId = context.boshFacade.createOpenStackServerGroup(context.lastOperationJobContext.provisionRequest.serviceInstanceGuid)
                    new ActionResult(success: true, details:[ ServiceDetail.from(ServiceDetailKey.CLOUD_PROVIDER_SERVER_GROUP_ID, serverGroupId)])
                }
            }).withStateAndAction(BoshProvisionState.CLOUD_PROVIDER_SERVER_GROUP_CREATED, new OnStateChange() {
                @Override
                ActionResult triggerAction(StateContext context) {
                    context.boshFacade.addOrUpdateVmInBoshCloudConfig(context.lastOperationJobContext)
                    return new ActionResult(success: true)
                }
            }).withStateAndAction(BoshProvisionState.BOSH_CLOUD_CONFIG_UPDATED, new OnStateChange() {
                @Override
                ActionResult triggerAction(StateContext context) {
                    new ActionResult(success: true, details: context.boshFacade.handleTemplatingAndCreateDeployment(context.lastOperationJobContext.provisionRequest, context.boshTemplateCustomizer))
                }
            }).withStateAndAction(BoshProvisionState.BOSH_DEPLOYMENT_TRIGGERED, new OnStateChange() {
                @Override
                ActionResult triggerAction(StateContext context) {
                    return new ActionResult(success: context.boshFacade.isBoshTaskSuccessful(context.lastOperationJobContext))
                }
            })
        }else{
            new StateFlow().withStateAndAction(BoshProvisionState.BOSH_INITIAL, new OnStateChange() {
                @Override
                ActionResult triggerAction(StateContext context) {
                    new ActionResult(success: true, details: context.boshFacade.handleTemplatingAndCreateDeployment(context.lastOperationJobContext.provisionRequest, context.boshTemplateCustomizer))
                }
            }).withStateAndAction(BoshProvisionState.BOSH_DEPLOYMENT_TRIGGERED, new OnStateChange() {
                @Override
                ActionResult triggerAction(StateContext context) {
                    return new ActionResult(success: context.boshFacade.isBoshTaskSuccessful(context.lastOperationJobContext))
                }
            })
        }

    }
}
