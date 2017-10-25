package com.swisscom.cloud.sb.broker.services.bosh.statemachine

import com.google.common.base.Optional
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.services.bosh.BoshFacade
import com.swisscom.cloud.sb.broker.services.bosh.BoshServiceDetailKey
import spock.lang.Specification

class BoshDeprovisionStateSpec extends Specification {
    private BoshStateMachineContext context

    def setup(){
        context = new BoshStateMachineContext()
        context.boshFacade = Mock(BoshFacade)
    }

    def "DELETE_BOSH_DEPLOYMENT with existing deployment"(){
        given:
        def deploymentId = 'deploymentId'
        def taskId = 'taskId'
        context.lastOperationJobContext = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: [ServiceDetail.from(BoshServiceDetailKey.BOSH_DEPLOYMENT_ID,deploymentId)]))
        and:
        1 * context.boshFacade.deleteBoshDeploymentIfExists(context.lastOperationJobContext) >> Optional.of(taskId)
        when:
        def result = BoshDeprovisionState.DELETE_BOSH_DEPLOYMENT.triggerAction(context)
        then:
        result.go2NextState
        result.details.find({it.key ==  BoshServiceDetailKey.BOSH_TASK_ID_FOR_UNDEPLOY.key}).value == taskId
    }

    def "DELETE_BOSH_DEPLOYMENT with *N0* existing deployment"(){
        given:
        context.lastOperationJobContext = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: []))
        and:
        1 * context.boshFacade.deleteBoshDeploymentIfExists(context.lastOperationJobContext) >> Optional.absent()
        when:
        def result = BoshDeprovisionState.DELETE_BOSH_DEPLOYMENT.triggerAction(context)
        then:
        result.go2NextState
        result.details.find({it.key ==  BoshServiceDetailKey.BOSH_TASK_ID_FOR_UNDEPLOY.key}) == null
    }

    def "CHECK_BOSH_UNDEPLOY_TASK_STATE "(){
        given:
        context.lastOperationJobContext = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: []))
        and:
        1 * context.boshFacade.isBoshUndeployTaskSuccessful(context.lastOperationJobContext)>>isBoshUndeploySuccessful
        when:
        def result = BoshDeprovisionState.CHECK_BOSH_UNDEPLOY_TASK_STATE.triggerAction(context)
        then:
        result.go2NextState == go2NextState
        !result.details
        where:
        isBoshUndeploySuccessful | go2NextState
        true | true
        false | false
    }

    def "UPDATE_BOSH_CLOUD_CONFIG "(){
        given:
        context.lastOperationJobContext = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: []))
        and:
        1 * context.boshFacade.removeVmInBoshCloudConfig(context.lastOperationJobContext)
        when:
        def result = BoshDeprovisionState.UPDATE_BOSH_CLOUD_CONFIG.triggerAction(context)
        then:
        result.go2NextState
        !result.details
    }

    def "DELETE_OPEN_STACK_SERVER_GROUP"(){
        given:
        context.lastOperationJobContext = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: []))
        and:
        1 * context.boshFacade.deleteOpenStackServerGroupIfExists(context.lastOperationJobContext)
        when:
        def result = BoshDeprovisionState.DELETE_OPEN_STACK_SERVER_GROUP.triggerAction(context)
        then:
        result.go2NextState
        !result.details
    }
}
