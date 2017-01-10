package com.swisscom.cf.broker.services.bosh.statemachine

import com.google.common.base.Optional
import com.swisscom.cf.broker.model.ServiceDetail
import com.swisscom.cf.broker.model.ServiceInstance
import com.swisscom.cf.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cf.broker.services.bosh.BoshFacade
import com.swisscom.cf.broker.util.ServiceDetailKey
import spock.lang.Specification

class BoshDeprovisionStateSpec extends Specification {
    private BoshStateMachineContext context

    def setup(){
        context = new BoshStateMachineContext()
        context.boshFacade = Mock(BoshFacade)
    }

    def "BOSH_INITIAL with existing deployment"(){
        given:
        def deploymentId = 'deploymentId'
        def taskId = 'taskId'
        context.lastOperationJobContext = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: [ServiceDetail.from(ServiceDetailKey.BOSH_DEPLOYMENT_ID,deploymentId)]))
        and:
        1 * context.boshFacade.deleteBoshDeploymentIfExists(context.lastOperationJobContext) >> Optional.of(taskId)
        when:
        def result = BoshDeprovisionState.BOSH_INITIAL.triggerAction(context)
        then:
        result.go2NextState
        result.details.find({it.key ==  ServiceDetailKey.BOSH_TASK_ID_FOR_UNDEPLOY.key}).value == taskId
    }

    def "BOSH_INITIAL with *N0* existing deployment"(){
        given:
        context.lastOperationJobContext = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: []))
        and:
        1 * context.boshFacade.deleteBoshDeploymentIfExists(context.lastOperationJobContext) >> Optional.absent()
        when:
        def result = BoshDeprovisionState.BOSH_INITIAL.triggerAction(context)
        then:
        result.go2NextState
        result.details.find({it.key ==  ServiceDetailKey.BOSH_TASK_ID_FOR_UNDEPLOY.key}) == null
    }

    def "BOSH_DEPLOYMENT_DELETION_REQUESTED "(){
        given:
        context.lastOperationJobContext = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: []))
        and:
        1 * context.boshFacade.isBoshUndeployTaskSuccessful(context.lastOperationJobContext)>>isBoshUndeploySuccessful
        when:
        def result = BoshDeprovisionState.BOSH_DEPLOYMENT_DELETION_REQUESTED.triggerAction(context)
        then:
        result.go2NextState == go2NextState
        !result.details
        where:
        isBoshUndeploySuccessful | go2NextState
        true | true
        false | false
    }

    def "BOSH_TASK_SUCCESSFULLY_FINISHED "(){
        given:
        context.lastOperationJobContext = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: []))
        and:
        1 * context.boshFacade.removeVmInBoshCloudConfig(context.lastOperationJobContext)
        when:
        def result = BoshDeprovisionState.BOSH_TASK_SUCCESSFULLY_FINISHED.triggerAction(context)
        then:
        result.go2NextState
        !result.details
    }

    def "BOSH_CLOUD_CONFIG_UPDATED"(){
        given:
        context.lastOperationJobContext = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: []))
        and:
        1 * context.boshFacade.deleteOpenStackServerGroupIfExists(context.lastOperationJobContext)
        when:
        def result = BoshDeprovisionState.BOSH_CLOUD_CONFIG_UPDATED.triggerAction(context)
        then:
        result.go2NextState
        !result.details
    }

    def "BOSH_FINAL"(){
        given:
        context.lastOperationJobContext = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: []))

        when:
        def result = BoshDeprovisionState.BOSH_FINAL.triggerAction(context)
        then:
        result.go2NextState
        !result.details
        0 * _._
    }

}
