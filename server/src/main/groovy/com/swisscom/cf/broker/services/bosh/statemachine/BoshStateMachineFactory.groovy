package com.swisscom.cf.broker.services.bosh.statemachine

import com.swisscom.cf.broker.provisioning.statemachine.StateMachine
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

@CompileStatic
@TypeChecked
class BoshStateMachineFactory {
    static StateMachine createProvisioningStateFlow(boolean shouldCreateOpenStackServerGroup) {
        if (shouldCreateOpenStackServerGroup) {
            new StateMachine([BoshProvisionState.CREATE_OPEN_STACK_SERVER_GROUP,
                              BoshProvisionState.UPDATE_BOSH_CLOUD_CONFIG,
                              BoshProvisionState.CREATE_DEPLOYMENT,
                              BoshProvisionState.CHECK_BOSH_DEPLOYMENT_TASK_STATE])
        } else {
            new StateMachine([BoshProvisionState.CREATE_DEPLOYMENT,
                              BoshProvisionState.CHECK_BOSH_DEPLOYMENT_TASK_STATE])
        }
    }

    static StateMachine createDeprovisioningStateFlow(boolean shouldCreateOpenStackServerGroup) {
        if (shouldCreateOpenStackServerGroup) {
            new StateMachine([BoshDeprovisionState.DELETE_BOSH_DEPLOYMENT,
                              BoshDeprovisionState.CHECK_BOSH_UNDEPLOY_TASK_STATE,
                              BoshDeprovisionState.UPDATE_BOSH_CLOUD_CONFIG,
                              BoshDeprovisionState.DELETE_OPEN_STACK_SERVER_GROUP])
        } else {
            new StateMachine([BoshDeprovisionState.DELETE_BOSH_DEPLOYMENT,
                              BoshDeprovisionState.CHECK_BOSH_UNDEPLOY_TASK_STATE])
        }
    }
}
