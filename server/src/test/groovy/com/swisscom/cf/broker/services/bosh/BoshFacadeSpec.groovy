package com.swisscom.cf.broker.services.bosh

import com.google.common.base.Optional
import com.swisscom.cf.broker.model.DeprovisionRequest
import com.swisscom.cf.broker.model.Parameter
import com.swisscom.cf.broker.model.Plan
import com.swisscom.cf.broker.model.ProvisionRequest
import com.swisscom.cf.broker.model.ServiceDetail
import com.swisscom.cf.broker.model.ServiceInstance
import com.swisscom.cf.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cf.broker.provisioning.lastoperation.LastOperationJobContextService
import com.swisscom.cf.broker.services.bosh.client.BoshClient
import com.swisscom.cf.broker.services.bosh.client.BoshClientFactory
import com.swisscom.cf.broker.services.bosh.dto.BoshInfoDto
import com.swisscom.cf.broker.services.bosh.dto.TaskDto
import com.swisscom.cf.broker.services.mongodb.enterprise.openstack.OpenStackClient
import com.swisscom.cf.broker.services.mongodb.enterprise.openstack.OpenStackClientFactory
import com.swisscom.cf.broker.util.ServiceDetailKey
import org.openstack4j.model.compute.ServerGroup
import spock.lang.Specification

class BoshFacadeSpec extends Specification {
    private def serverGroupId = "serverGroupId"
    private def boshDeploymentId = "boshDeploymentId"
    private def boshTaskId = "boshTaskId"
    public static final String serviceInstanceGuid = "serviceInstanceGuid"

    OpenStackClient openstackClient
    OpenStackClientFactory openStackClientFactory
    BoshClient boshClient
    BoshClientFactory boshClientFactory
    BoshBasedServiceConfig serviceConfig
    BoshFacade boshFacade
    BoshTemplateFactory boshTemplateFactory
    BoshTemplate boshTemplate

    void setup() {
        openstackClient = Mock(OpenStackClient)
        openStackClientFactory = Mock(OpenStackClientFactory)
        openStackClientFactory.createOpenStackClient(*_) >> openstackClient
        and:
        boshClient = Mock(BoshClient)
        boshClientFactory = Mock(BoshClientFactory)
        boshClientFactory.build(*_) >> boshClient
        and:
        serviceConfig = new DummyConfig(retryIntervalInSeconds: 1, maxRetryDurationInMinutes: 1)
        and:
        boshTemplate = Mock(BoshTemplate)
        boshTemplateFactory = Mock(BoshTemplateFactory) { build(_) >> boshTemplate }
        and:
        boshFacade = new BoshFacade(boshClientFactory, openStackClientFactory, serviceConfig, boshTemplateFactory)
    }

    def "host name creation works correctly"() {
        when:
        def result = boshFacade.generateHostNames('guid', 3)
        then:
        result.size() == 3
        result.contains("guid-0${BoshFacade.HOST_NAME_POSTFIX}")
    }

    def "creating OpenStack server group works correctly"(){
        given:
        def groupName = 'groupName'
        1 * openstackClient.createAntiAffinityServerGroup(groupName)>> new DummyServerGroup(id:serverGroupId)
        expect:
        boshFacade.createOpenStackServerGroup(groupName) == serverGroupId
    }


    def "happy path: addOrUpdateVm"(){
        given:
        def id = 'id'
        def vmType = 'vmType'
        def serverGroupId = 'serverGroupId'
        def context = new LastOperationJobContext(provisionRequest: new ProvisionRequest(serviceInstanceGuid: id),
                                                    plan: new Plan(parameters: [new Parameter(name:BoshFacade.PARAM_BOSH_VM_INSTANCE_TYPE,value: vmType)]),
                                                    serviceInstance: new ServiceInstance(details: [ServiceDetail.from(ServiceDetailKey.CLOUD_PROVIDER_SERVER_GROUP_ID, serverGroupId)]))
        when:
        boshFacade.addOrUpdateVmInBoshCloudConfig(context)
        then:
        1 * boshClient.addOrUpdateVmInCloudConfig(id,vmType,serverGroupId)
    }

    def "findServerGroupId works for service instance with existing detail"(){
        given:
        def context = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: [ServiceDetail.from(ServiceDetailKey.CLOUD_PROVIDER_SERVER_GROUP_ID, serverGroupId)]))
        expect:
        serverGroupId == boshFacade.findServerGroupId(context).get()
    }

    def "findServerGroupId  queries the OpenStack endpoint to find the group id"(){
        given:
        def guid = 'guid'
        def context = new LastOperationJobContext(serviceInstance: new ServiceInstance(guid: guid))
        and:
        1 * openstackClient.findServerGroup(context.serviceInstance.guid)>>Optional.of( new DummyServerGroup(id:serverGroupId))
        expect:
        serverGroupId == boshFacade.findServerGroupId(context).get()
    }

    def "findServerGroupId returns empty optional when group id can't be found"(){
        given:
        def guid = 'guid'
        def context = new LastOperationJobContext(serviceInstance: new ServiceInstance(guid: guid))
        and:
        1 * openstackClient.findServerGroup(context.serviceInstance.guid)>>Optional.absent()
        expect:
        !boshFacade.findServerGroupId(context).present
    }

    def "templating is handled correctly"(){
        given:
        def request = new ProvisionRequest(serviceInstanceGuid:"guid",plan: new Plan(templateUniqueIdentifier: '/bosh/template_mongodbent_v5.yml',
                                                                                        parameters: [new Parameter(name: 'name1',value: 'value1')]))
        def customizer = Mock(BoshTemplateCustomizer)
        and:
        def boshUuid = 'boshUuid'
        1 * boshClient.fetchBoshInfo() >> new BoshInfoDto(uuid: boshUuid)
        and:
        def serviceDetails = [new ServiceDetail()]
        1 * customizer.customizeBoshTemplate(boshTemplate,request) >> serviceDetails
        and:
        1 * boshClient.postDeployment(_)>>'taskId'
        when:
        def result = boshFacade.handleTemplatingAndCreateDeployment(request,customizer)
        then:
        1 * boshTemplate.replace('guid', request.serviceInstanceGuid )
        1 * boshTemplate.replace('name1','value1')
        result.size() == 3
    }

    def "bosh Deploy task state checking is handled correctly for normal cases"(){
        given:
        def taskId = 'taskId'
        def context = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: [ServiceDetail.from(ServiceDetailKey.BOSH_TASK_ID_FOR_DEPLOY, taskId)]))
        and:
        1 * boshClient.getTask(taskId)>> new TaskDto(state:boskTaskState)
        expect:
        boshFacade.isBoshDeployTaskSuccessful(context) == result

        where:
        result | boskTaskState
        true | TaskDto.State.done
        false | TaskDto.State.processing
    }

    def "bosh task state checking is handled correctly for normal cases"(){
        given:
        def taskId = 'taskId'
        and:
        1 * boshClient.getTask(taskId)>> new TaskDto(state:boskTaskState)
        expect:
        boshFacade.isBoshTaskSuccessful(taskId) == result

        where:
        result | boskTaskState
        true | TaskDto.State.done
        false | TaskDto.State.processing
    }

    def "bosh Deploy task state checking is handled correctly for error cases"(){
        given:
        def taskId = 'taskId'
        and:
        1 * boshClient.getTask(taskId)>> new TaskDto(state:boskTaskState)
        when:
        boshFacade.isBoshTaskSuccessful(taskId)
        then:
        def ex = thrown(RuntimeException)
        where:
        boskTaskState<<[TaskDto.State.cancelled,TaskDto.State.cancelling,TaskDto.State.errored]
    }

    def "bosh Deploy task state checking is handled correctly for error state can not be parsed"(){
        given:
        def taskId = 'taskId'
        and:
        1 * boshClient.getTask(taskId)>> new TaskDto(state:null)
        when:
        boshFacade.isBoshTaskSuccessful(taskId)
        then:
        def ex = thrown(RuntimeException)
    }

    def "bosh UNDeploy task state checking is handled correctly for normal cases"(){
        given:
        def taskId = 'taskId'
        def context = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: [ServiceDetail.from(ServiceDetailKey.BOSH_TASK_ID_FOR_UNDEPLOY, taskId)]))
        and:
        1 * boshClient.getTask(taskId)>> new TaskDto(state:boskTaskState)
        expect:
        boshFacade.isBoshUndeployTaskSuccessful(context) == result

        where:
        result | boskTaskState
        true | TaskDto.State.done
        false | TaskDto.State.processing
    }

    def "deleting bosh deployment works correctly when bosh deployment id can be found in service details"(){
        given:
        Optional<String> taskId = Optional.absent()
        def deploymentId = "deploymentId"
        def context = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: [ServiceDetail.from(ServiceDetailKey.BOSH_DEPLOYMENT_ID,deploymentId)]))
        1 * boshClient.deleteDeploymentIfExists(deploymentId)>>taskId

        expect:
        boshFacade.deleteBoshDeploymentIfExists(context) == taskId
    }

    def "deleting bosh deployment works correctly when bosh deployment id is *NOT*found in service details"(){
        given:
        Optional<String> taskId = Optional.absent()
        String serviceInstanceGuid = 'serviceInstanceGuid'
        def deploymentId = boshFacade.generateDeploymentId(serviceInstanceGuid)
        def context = new LastOperationJobContext(serviceInstance: new ServiceInstance(guid: serviceInstanceGuid))
        1 * boshClient.deleteDeploymentIfExists(deploymentId)>>taskId

        expect:
        boshFacade.deleteBoshDeploymentIfExists(context) == taskId
    }

    def "removing vm in cloud config functions correctly"(){
        given:
        def context = new LastOperationJobContext(deprovisionRequest: new DeprovisionRequest(serviceInstanceGuid: 'serviceInstanceGuid'))

        when:
        boshFacade.removeVmInBoshCloudConfig(context)

        then:
        1 *  boshClient.removeVmInCloudConfig(context.deprovisionRequest.serviceInstanceGuid)
    }

    def "delete existing OpenStack group"(){
        given:
        def context = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: [ServiceDetail.from(ServiceDetailKey.CLOUD_PROVIDER_SERVER_GROUP_ID,serverGroupId)]))

        when:
        boshFacade.deleteOpenStackServerGroupIfExists(context)

        then:
        1 * openstackClient.deleteServerGroup(serverGroupId)
    }

    private static class DummyServerGroup implements ServerGroup{
        String id
        String name
        List<String> members
        Map<String, String> metadata
        List<String> policies
    }
}
