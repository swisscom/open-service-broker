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
                              BoshProvisionState.BOSH_DEPLOYMENT_TRIGGERED])
        } else {
            new StateMachine([BoshProvisionState.CREATE_DEPLOYMENT,
                              BoshProvisionState.BOSH_DEPLOYMENT_TRIGGERED])
        }
    }

    static StateMachine createDeprovisioningStateFlow(boolean shouldCreateOpenStackServerGroup) {
        if (shouldCreateOpenStackServerGroup) {
            new StateMachine([BoshDeprovisionState.BOSH_INITIAL,
                              BoshDeprovisionState.BOSH_DEPLOYMENT_DELETION_REQUESTED,
                              BoshDeprovisionState.BOSH_TASK_SUCCESSFULLY_FINISHED,
                              BoshDeprovisionState.BOSH_CLOUD_CONFIG_UPDATED,
                              BoshDeprovisionState.BOSH_FINAL])
        } else {
            new StateMachine([BoshDeprovisionState.BOSH_INITIAL,
                              BoshDeprovisionState.BOSH_DEPLOYMENT_DELETION_REQUESTED,
                              BoshDeprovisionState.BOSH_FINAL])
        }
    }
}
