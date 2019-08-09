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

import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.services.bosh.BoshFacade
import com.swisscom.cloud.sb.broker.services.bosh.BoshTemplateCustomizer
import spock.lang.Specification

class BoshProvisionStateSpec extends Specification {
    private BoshStateMachineContext context

    def setup() {
        context = new BoshStateMachineContext()
        context.boshFacade = Mock(BoshFacade)
    }

    def "CREATE_DEPLOYMENT"() {
        given:
        context.lastOperationJobContext = new LastOperationJobContext(provisionRequest: new ProvisionRequest(
                serviceInstanceGuid: "guid", plan: new Plan(templateUniqueIdentifier: "test")))
        context.boshTemplateCustomizer = Mock(BoshTemplateCustomizer)
        def details = [ServiceDetail.from("key", "value")]
        and:
        1 * context.boshFacade.handleTemplatingAndCreateDeployment("guid",
                                                                   "test",
                                                                   [].toSet(),
                                                                   context.boshTemplateCustomizer) >> details
        when:
        def result = BoshProvisionState.CREATE_DEPLOYMENT.triggerAction(context)
        then:
        result.go2NextState
        result.details == details
    }

    def "CHECK_BOSH_DEPLOYMENT_TASK_STATE "() {
        given:
        context.lastOperationJobContext = new LastOperationJobContext(provisionRequest: new ProvisionRequest(
                serviceInstanceGuid: "guid"))
        and:
        1 * context.boshFacade.isBoshDeployTaskSuccessful(context.lastOperationJobContext.serviceInstance.details) >> isBoshDeploySuccessful
        when:
        def result = BoshProvisionState.CHECK_BOSH_DEPLOYMENT_TASK_STATE.triggerAction(context)
        then:
        result.go2NextState == go2NextState
        !result.details
        where:
        isBoshDeploySuccessful | go2NextState
        true                   | true
        false                  | false
    }
}
