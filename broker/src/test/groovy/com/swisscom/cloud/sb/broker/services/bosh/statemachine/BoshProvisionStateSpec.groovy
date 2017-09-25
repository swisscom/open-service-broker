package com.swisscom.cloud.sb.broker.services.bosh.statemachine

import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.services.bosh.BoshFacade
import com.swisscom.cloud.sb.broker.services.bosh.BoshServiceDetailKey
import com.swisscom.cloud.sb.broker.services.bosh.BoshTemplateCustomizer
import spock.lang.Specification

class BoshProvisionStateSpec extends Specification {
    private BoshStateMachineContext context

    def setup(){
        context = new BoshStateMachineContext()
        context.boshFacade = Mock(BoshFacade)
    }

    def "CREATE_OPEN_STACK_SERVER_GROUP"(){
        given:
        def openStackGroupId = 'openStackGroupId'
        context.lastOperationJobContext = new LastOperationJobContext(provisionRequest: new ProvisionRequest(serviceInstanceGuid: 'guid'))
        and:
        1 * context.boshFacade.createOpenStackServerGroup('guid') >> openStackGroupId
        when:
        def result = BoshProvisionState.CREATE_OPEN_STACK_SERVER_GROUP.triggerAction(context)
        then:
        result.go2NextState
        result.details.find({it.key ==  BoshServiceDetailKey.CLOUD_PROVIDER_SERVER_GROUP_ID.key}).value == openStackGroupId
    }

    def "UPDATE_BOSH_CLOUD_CONFIG"(){
        given:
        context.lastOperationJobContext = new LastOperationJobContext(provisionRequest: new ProvisionRequest(serviceInstanceGuid: 'guid'))
        and:
        1 * context.boshFacade.addOrUpdateVmInBoshCloudConfig(context.lastOperationJobContext)
        when:
        def result = BoshProvisionState.UPDATE_BOSH_CLOUD_CONFIG.triggerAction(context)
        then:
        result.go2NextState
        !result.details
    }

    def "CREATE_DEPLOYMENT"(){
        given:
        context.lastOperationJobContext = new LastOperationJobContext(provisionRequest: new ProvisionRequest(serviceInstanceGuid: 'guid'))
        context.boshTemplateCustomizer = Mock(BoshTemplateCustomizer)
        def details = [ServiceDetail.from('key','value')]
        and:
        1 * context.boshFacade.handleTemplatingAndCreateDeployment(context.lastOperationJobContext.provisionRequest,context.boshTemplateCustomizer) >> details
        when:
        def result = BoshProvisionState.CREATE_DEPLOYMENT.triggerAction(context)
        then:
        result.go2NextState
        result.details == details
    }

    def "CHECK_BOSH_DEPLOYMENT_TASK_STATE "(){
        given:
        context.lastOperationJobContext = new LastOperationJobContext(provisionRequest: new ProvisionRequest(serviceInstanceGuid: 'guid'))
        and:
        1 * context.boshFacade.isBoshDeployTaskSuccessful(context.lastOperationJobContext)>>isBoshDeploySuccessful
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
