package com.swisscom.cf.broker.services.bosh

import com.google.common.base.Optional
import com.swisscom.cf.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cf.broker.model.*
import com.swisscom.cf.broker.services.bosh.statemachine.BoshDeprovisionState
import com.swisscom.cf.broker.services.bosh.statemachine.BoshProvisionState
import com.swisscom.cf.broker.services.mongodb.enterprise.openstack.OpenStackClient
import com.swisscom.cf.broker.services.mongodb.enterprise.openstack.OpenStackClientFactory
import com.swisscom.cf.broker.services.bosh.client.BoshClient
import com.swisscom.cf.broker.services.bosh.client.BoshClientFactory
import com.swisscom.cf.broker.services.bosh.dto.BoshInfoDto
import com.swisscom.cf.broker.services.bosh.dto.TaskDto
import com.swisscom.cf.broker.util.ServiceDetailKey
import com.swisscom.cf.broker.util.ServiceDetailsHelper
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

    def "requestProvision(when no matching state is found, an empty response should be returned)"() {
        when:
        def result = boshFacade.handleBoshProvisioning(new LastOperationJobContext(lastOperation: new LastOperation(internalState: 'noSuch State')), null)
        then:
        !result.isPresent()
    }

    def "requestProvision(when internalState is empty, it should start from BOSH_INITIAL)"() {
        given:
        1 * openstackClient.createAntiAffinityServerGroup(serviceInstanceGuid) >> Stub(ServerGroup) {
            getId() >> serverGroupId
        }
        when:
        def result = boshFacade.handleBoshProvisioning(new LastOperationJobContext(lastOperation: new LastOperation(internalState: null), provisionRequest: new ProvisionRequest(serviceInstanceGuid: serviceInstanceGuid)), null)
        then:
        result.get().internalStatus == BoshProvisionState.CLOUD_PROVIDER_SERVER_GROUP_CREATED.toString()
        ServiceDetailsHelper.from(result.get().details).getValue(ServiceDetailKey.CLOUD_PROVIDER_SERVER_GROUP_ID) == serverGroupId
    }

    def "requestProvision(internalState: CLOUD_PROVIDER_SERVER_GROUP_CREATED)"() {
        given:
        def vmType = "small"
        def context = new LastOperationJobContext(lastOperation: new LastOperation(internalState: BoshProvisionState.CLOUD_PROVIDER_SERVER_GROUP_CREATED.toString()),
                provisionRequest: new ProvisionRequest(serviceInstanceGuid: serviceInstanceGuid),
                serviceInstance: new ServiceInstance(details: [ServiceDetail.from(ServiceDetailKey.CLOUD_PROVIDER_SERVER_GROUP_ID, serverGroupId)]),
                plan: new Plan(parameters: [new Parameter(name: BoshFacade.PARAM_BOSH_VM_INSTANCE_TYPE, value: vmType)]))

        when:
        def result = boshFacade.handleBoshProvisioning(context, null)
        then:
        1 * boshClient.addOrUpdateVmInCloudConfig(context.provisionRequest.serviceInstanceGuid, vmType, serverGroupId)
        result.get().internalStatus == BoshProvisionState.BOSH_CLOUD_CONFIG_UPDATED.toString()
    }

    def "requestProvision(internalState: BOSH_CLOUD_CONFIG_UPDATED)"() {
        given:
        def context = new LastOperationJobContext(lastOperation: new LastOperation(internalState: BoshProvisionState.BOSH_CLOUD_CONFIG_UPDATED.toString()),
                provisionRequest: new ProvisionRequest(serviceInstanceGuid: serviceInstanceGuid, plan: new Plan(templateUniqueIdentifier: 'src/test/resources/bosh/template_redis_ha_v10.yml')),
                serviceInstance: new ServiceInstance(details: [ServiceDetail.from(ServiceDetailKey.CLOUD_PROVIDER_SERVER_GROUP_ID, serverGroupId)]))

        and:
        def boshTaskId = 'boshTaskId'
        1 * boshClient.postDeployment(_) >> boshTaskId
        and:
        BoshTemplateCustomizer templateCustomizer = Mock(BoshTemplateCustomizer)
        1 * templateCustomizer.customizeBoshTemplate(_, context.provisionRequest)
        and:
        1 * boshClient.fetchBoshInfo() >> new BoshInfoDto(uuid: "adsfasdfasdf")
        when:
        def result = boshFacade.handleBoshProvisioning(context, templateCustomizer)
        then:
        result.get().internalStatus == BoshProvisionState.BOSH_DEPLOYMENT_TRIGGERED.toString()
        ServiceDetailsHelper.from(result.get().details).getValue(ServiceDetailKey.BOSH_TASK_ID_FOR_DEPLOY) == boshTaskId
    }

    def "requestProvision(internalState: BOSH_DEPLOYMENT_TRIGGERED but not successful)"() {
        given:
        def boshTaskId = 'boshTaskId'
        def context = new LastOperationJobContext(lastOperation: new LastOperation(internalState: BoshProvisionState.BOSH_DEPLOYMENT_TRIGGERED.toString()),
                provisionRequest: new ProvisionRequest(serviceInstanceGuid: serviceInstanceGuid),
                serviceInstance: new ServiceInstance(details: [ServiceDetail.from(ServiceDetailKey.BOSH_TASK_ID_FOR_DEPLOY, boshTaskId)]))
        and:
        1 * boshClient.getTask(boshTaskId) >> new TaskDto(state: TaskDto.State.processing)

        when:
        def result = boshFacade.handleBoshProvisioning(context, null)
        then:
        result.get().internalStatus == BoshProvisionState.BOSH_DEPLOYMENT_TRIGGERED.toString()
    }

    def "requestProvision(internalState: BOSH_TASK_SUCCESSFULLY_FINISHED)"() {
        given:
        def boshTaskId = 'boshTaskId'
        def context = new LastOperationJobContext(lastOperation: new LastOperation(internalState: BoshProvisionState.BOSH_DEPLOYMENT_TRIGGERED.toString()),
                provisionRequest: new ProvisionRequest(serviceInstanceGuid: serviceInstanceGuid),
                serviceInstance: new ServiceInstance(details: [ServiceDetail.from(ServiceDetailKey.BOSH_TASK_ID_FOR_DEPLOY, boshTaskId)]))
        and:
        1 * boshClient.getTask(boshTaskId) >> new TaskDto(state: TaskDto.State.done)

        when:
        def result = boshFacade.handleBoshProvisioning(context, null)
        then:
        result.get().internalStatus == BoshProvisionState.BOSH_TASK_SUCCESSFULLY_FINISHED.toString()
    }

    def "requestDeprovision(when no matching state is found, an empty response should be returned)"() {
        when:
        def result = boshFacade.handleBoshDeprovisioning(new LastOperationJobContext(lastOperation: new LastOperation(internalState: 'noSuch State')))
        then:
        !result.isPresent()
    }

    def "requestDeprovision(when internalState is empty, it should start from BOSH_INITIAL"() {
        given:
        def context = new LastOperationJobContext(lastOperation: new LastOperation(internalState: null),
                provisionRequest: new ProvisionRequest(serviceInstanceGuid: 'serviceInstanceGuid'),
                serviceInstance: new ServiceInstance(details: [ServiceDetail.from(ServiceDetailKey.BOSH_DEPLOYMENT_ID, boshDeploymentId)]))
        and:
        1 * boshClient.deleteDeploymentIfExists(boshDeploymentId) >> Optional.of(boshTaskId)
        when:
        def result = boshFacade.handleBoshDeprovisioning(context)
        then:
        result.get().internalStatus == BoshDeprovisionState.BOSH_DEPLOYMENT_DELETION_REQUESTED.toString()
        ServiceDetailsHelper.from(result.get().details).getValue(ServiceDetailKey.BOSH_TASK_ID_FOR_UNDEPLOY) == boshTaskId
    }

    def "requestDeprovision(internalState: BOSH_DEPLOYMENT_DELETION_REQUESTED and bosh task state is ongoing)"() {
        given:
        def context = new LastOperationJobContext(lastOperation: new LastOperation(internalState: BoshDeprovisionState.BOSH_DEPLOYMENT_DELETION_REQUESTED.toString()),
                provisionRequest: new ProvisionRequest(serviceInstanceGuid: 'serviceInstanceGuid'),
                serviceInstance: new ServiceInstance(details: [ServiceDetail.from(ServiceDetailKey.BOSH_TASK_ID_FOR_UNDEPLOY, boshTaskId)]))
        and:
        1 * boshClient.getTask(boshTaskId) >> new TaskDto(state: TaskDto.State.processing)
        when:
        def result = boshFacade.handleBoshDeprovisioning(context)
        then:
        result.get().internalStatus == BoshDeprovisionState.BOSH_DEPLOYMENT_DELETION_REQUESTED.toString()
    }

    def "requestDeprovision(internalState: BOSH_DEPLOYMENT_DELETION_REQUESTED and bosh task state is done)"() {
        given:
        def context = new LastOperationJobContext(lastOperation: new LastOperation(internalState: BoshDeprovisionState.BOSH_DEPLOYMENT_DELETION_REQUESTED.toString()),
                provisionRequest: new ProvisionRequest(serviceInstanceGuid: 'serviceInstanceGuid'),
                serviceInstance: new ServiceInstance(details: [ServiceDetail.from(ServiceDetailKey.BOSH_TASK_ID_FOR_UNDEPLOY, boshTaskId)]))
        and:
        1 * boshClient.getTask(boshTaskId) >> new TaskDto(state: TaskDto.State.done)
        when:
        def result = boshFacade.handleBoshDeprovisioning(context)
        then:
        result.get().internalStatus == BoshDeprovisionState.BOSH_TASK_SUCCESSFULLY_FINISHED.toString()
    }

    def "requestDeprovision(internalState: BOSH_TASK_SUCCESSFULLY_FINISHED)"() {
        given:
        def context = new LastOperationJobContext(lastOperation: new LastOperation(internalState: BoshDeprovisionState.BOSH_TASK_SUCCESSFULLY_FINISHED.toString()),
                deprovisionRequest: new DeprovisionRequest(serviceInstanceGuid: 'serviceInstanceGuid'))
        and:
        1 * boshClient.removeVmInCloudConfig(context.deprovisionRequest.serviceInstanceGuid)
        when:
        def result = boshFacade.handleBoshDeprovisioning(context)
        then:
        result.get().internalStatus == BoshDeprovisionState.BOSH_CLOUD_CONFIG_UPDATED.toString()
    }


    def "requestDeprovision(internalState: BOSH_CLOUD_CONFIG_UPDATED)"() {
        given:
        def context = new LastOperationJobContext(lastOperation: new LastOperation(internalState: BoshDeprovisionState.BOSH_CLOUD_CONFIG_UPDATED.toString()),
                provisionRequest: new ProvisionRequest(serviceInstanceGuid: 'serviceInstanceGuid'),
                serviceInstance: new ServiceInstance(details: [ServiceDetail.from(ServiceDetailKey.CLOUD_PROVIDER_SERVER_GROUP_ID, serverGroupId)]))
        and:
        1 * openstackClient.deleteServerGroup(serverGroupId)
        when:
        def result = boshFacade.handleBoshDeprovisioning(context)
        then:
        result.get().internalStatus == BoshDeprovisionState.CLOUD_PROVIDER_SERVER_GROUP_DELETED.toString()
    }


    def "host name creation works correctly"() {
        when:
        def result = boshFacade.generateHostNames('guid', 3)
        then:
        result.size() == 3
        result.contains("guid-0${BoshFacade.HOST_NAME_POSTFIX}")
    }

}
