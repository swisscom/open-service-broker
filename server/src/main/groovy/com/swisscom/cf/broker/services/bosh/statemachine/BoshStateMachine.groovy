package com.swisscom.cf.broker.services.bosh.statemachine

import com.swisscom.cf.broker.provisioning.statemachine.StateMachine
import com.swisscom.cf.broker.services.bosh.BoshDeprovisionState
import com.swisscom.cf.broker.services.bosh.BoshProvisionState
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

@CompileStatic
@TypeChecked
class BoshStateMachine {

    static StateMachine createProvisioningStateFlow(boolean shouldCreateOpenStackServerGroup) {
        if (shouldCreateOpenStackServerGroup) {
            new StateMachine().addState(BoshProvisionState.BOSH_INITIAL)
                                .addState(BoshProvisionState.CLOUD_PROVIDER_SERVER_GROUP_CREATED)
                                .addState(BoshProvisionState.BOSH_CLOUD_CONFIG_UPDATED)
                                .addState(BoshProvisionState.BOSH_DEPLOYMENT_TRIGGERED)
        }else{
            new StateMachine().addState(BoshProvisionState.BOSH_INITIAL)
                                .addState(BoshProvisionState.BOSH_DEPLOYMENT_TRIGGERED)
        }
    }

    static StateMachine createDeprovisioningStateFlow(boolean shouldCreateOpenStackServerGroup) {
        if (shouldCreateOpenStackServerGroup) {
            new StateMachine().addState(BoshDeprovisionState.BOSH_INITIAL)
                                .addState(BoshDeprovisionState.BOSH_DEPLOYMENT_DELETION_REQUESTED)
                                .addState(BoshDeprovisionState.BOSH_TASK_SUCCESSFULLY_FINISHED)
                                .addState(BoshDeprovisionState.BOSH_CLOUD_CONFIG_UPDATED)
                                .addState(BoshDeprovisionState.BOSH_FINAL)
        }else{
            new StateMachine().addState(BoshDeprovisionState.BOSH_INITIAL)
                                .addState(BoshDeprovisionState.BOSH_DEPLOYMENT_DELETION_REQUESTED)
                                .addState(BoshDeprovisionState.BOSH_FINAL)
        }
    }

}
