package com.swisscom.cf.broker.services.bosh.statemachine

import com.swisscom.cf.broker.model.ProvisionRequest
import com.swisscom.cf.broker.model.ServiceDetail
import com.swisscom.cf.broker.model.ServiceInstance
import com.swisscom.cf.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cf.broker.services.bosh.BoshFacade
import com.swisscom.cf.broker.services.bosh.BoshTemplateCustomizer
import com.swisscom.cf.broker.util.ServiceDetailKey
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
        result.details.find({it.key ==  ServiceDetailKey.CLOUD_PROVIDER_SERVER_GROUP_ID.key}).value == openStackGroupId
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

    def "BOSH_DEPLOYMENT_DELETION_REQUESTED "(){
        given:
        context.lastOperationJobContext = new LastOperationJobContext(provisionRequest: new ProvisionRequest(serviceInstanceGuid: 'guid'))
        and:
        1 * context.boshFacade.isBoshDeployTaskSuccessful(context.lastOperationJobContext)>>isBoshDeploySuccessful
        when:
        def result = BoshProvisionState.BOSH_DEPLOYMENT_TRIGGERED.triggerAction(context)
        then:
        result.go2NextState == go2NextState
        !result.details
        where:
        isBoshDeploySuccessful | go2NextState
        true                   | true
        false                  | false
    }

    def "BOSH_TASK_SUCCESSFULLY_FINISHED"(){
        given:
        context.lastOperationJobContext = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: []))

        when:
        def result = BoshProvisionState.BOSH_TASK_SUCCESSFULLY_FINISHED.triggerAction(context)
        then:
        result.go2NextState
        !result.details
        0 * _._
    }

}
