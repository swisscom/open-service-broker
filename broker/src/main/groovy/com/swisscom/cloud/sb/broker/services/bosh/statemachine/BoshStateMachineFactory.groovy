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

import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateMachine
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
