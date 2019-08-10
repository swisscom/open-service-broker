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
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.services.bosh.BoshFacade
import com.swisscom.cloud.sb.broker.services.bosh.BoshServiceDetailKey
import spock.lang.Specification

import static com.google.common.base.Optional.of

class BoshDeprovisionStateSpec extends Specification {
    private BoshStateMachineContext context

    def setup() {
        context = new BoshStateMachineContext()
        context.boshFacade = Mock(BoshFacade)
    }

    def "DELETE_BOSH_DEPLOYMENT with existing deployment"() {
        given:
        def deploymentId = 'deploymentId'
        def taskId = 'taskId'
        context.lastOperationJobContext = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: [ServiceDetail.
                from(BoshServiceDetailKey.BOSH_DEPLOYMENT_ID, deploymentId)]))
        and:
        1 * context.boshFacade.
                deleteBoshDeploymentIfExists(context.lastOperationJobContext.serviceInstance.details,
                                             context.lastOperationJobContext.serviceInstance.guid) >> of(taskId)
        when:
        def result = BoshDeprovisionState.DELETE_BOSH_DEPLOYMENT.triggerAction(context)
        then:
        result.go2NextState
        result.details.find({it.key == BoshServiceDetailKey.BOSH_TASK_ID_FOR_UNDEPLOY.key}).value == taskId
    }

    def "DELETE_BOSH_DEPLOYMENT with *N0* existing deployment"() {
        given:
        context.lastOperationJobContext = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: []))
        and:
        1 * context.boshFacade.
                deleteBoshDeploymentIfExists(context.lastOperationJobContext.serviceInstance.details,
                                             context.lastOperationJobContext.serviceInstance.guid) >> Optional.absent()
        when:
        def result = BoshDeprovisionState.DELETE_BOSH_DEPLOYMENT.triggerAction(context)
        then:
        result.go2NextState
        result.details.find({it.key == BoshServiceDetailKey.BOSH_TASK_ID_FOR_UNDEPLOY.key}) == null
    }

    def "CHECK_BOSH_UNDEPLOY_TASK_STATE "() {
        given:
        context.lastOperationJobContext = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: []))
        and:
        1 * context.boshFacade.isBoshUndeployTaskSuccessful(context.lastOperationJobContext.serviceInstance.details) >> isBoshUndeploySuccessful
        when:
        def result = BoshDeprovisionState.CHECK_BOSH_UNDEPLOY_TASK_STATE.triggerAction(context)
        then:
        result.go2NextState == go2NextState
        !result.details
        where:
        isBoshUndeploySuccessful | go2NextState
        true                     | true
        false                    | false
    }
}
