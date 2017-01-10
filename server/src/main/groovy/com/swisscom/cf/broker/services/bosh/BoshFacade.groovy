package com.swisscom.cf.broker.services.bosh

import com.google.common.annotations.VisibleForTesting
import com.google.common.base.Optional
import com.google.common.base.Preconditions
import com.swisscom.cf.broker.model.Parameter
import com.swisscom.cf.broker.model.ProvisionRequest
import com.swisscom.cf.broker.model.ServiceDetail
import com.swisscom.cf.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cf.broker.services.bosh.client.BoshClient
import com.swisscom.cf.broker.services.bosh.client.BoshClientFactory
import com.swisscom.cf.broker.services.bosh.dto.TaskDto
import com.swisscom.cf.broker.services.mongodb.enterprise.openstack.OpenStackClient
import com.swisscom.cf.broker.services.mongodb.enterprise.openstack.OpenStackClientFactory
import com.swisscom.cf.broker.util.ServiceDetailKey
import com.swisscom.cf.broker.util.ServiceDetailsHelper
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

@CompileStatic
@Log4j
class BoshFacade {
    public static final String PARAM_BOSH_VM_INSTANCE_TYPE = 'vm_instance_type'
    public static final String DEPLOYMENT_PREFIX = 'd-'
    public static final String HOST_NAME_POSTFIX = '.service.consul'
    public static final String PARAM_BOSH_DIRECTOR_UUID = "bosh-director-uuid"
    public static final String PARAM_PREFIX = 'prefix'

    private final BoshClientFactory boshClientFactory
    private final OpenStackClientFactory openStackClientFactory
    private final BoshBasedServiceConfig serviceConfig
    private final BoshTemplateFactory boshTemplateFactory

    BoshFacade(BoshClientFactory boshClientFactory, OpenStackClientFactory openStackClientFactory, BoshBasedServiceConfig serviceConfig, BoshTemplateFactory boshTemplateFactory) {
        this.boshClientFactory = boshClientFactory
        this.openStackClientFactory = openStackClientFactory
        this.serviceConfig = serviceConfig
        this.boshTemplateFactory = boshTemplateFactory
    }

    String createOpenStackServerGroup(String name) {
        return createOpenstackClient().createAntiAffinityServerGroup(name).id
    }

    private OpenStackClient createOpenstackClient() {
        return openStackClientFactory.createOpenStackClient(serviceConfig.openstackkUrl,
                serviceConfig.openstackUsername, serviceConfig.openstackPassword, serviceConfig.openstackTenantName)
    }

    def addOrUpdateVmInBoshCloudConfig(LastOperationJobContext context) {
        createBoshClient().addOrUpdateVmInCloudConfig(context.provisionRequest.serviceInstanceGuid, findBoshVmInstanceType(context), findServerGroupId(context).get())
    }

    private Optional<String> findServerGroupId(LastOperationJobContext context) {
        Optional<String> maybeGroupId = ServiceDetailsHelper.from(context.serviceInstance.details).findValue(ServiceDetailKey.CLOUD_PROVIDER_SERVER_GROUP_ID)
        if (maybeGroupId.present) {
            return maybeGroupId
        } else {
            def maybeServerGroup = createOpenstackClient().findServerGroup(context.serviceInstance.guid)
            if (maybeServerGroup.present) {
                return Optional.of(maybeServerGroup.get().id)
            } else {
                log.warn("ServerGroup Id not found for serviceInstance:${context.serviceInstance.guid}")
                return Optional.absent()
            }
        }
    }

    private static String findBoshTaskIdForDeploy(LastOperationJobContext context) {
        return ServiceDetailsHelper.from(context.serviceInstance.details).getValue(ServiceDetailKey.BOSH_TASK_ID_FOR_DEPLOY)
    }

    private static String findBoshTaskIdForUndeploy(LastOperationJobContext context) {
        return ServiceDetailsHelper.from(context.serviceInstance.details).getValue(ServiceDetailKey.BOSH_TASK_ID_FOR_UNDEPLOY)
    }

    private static String findBoshVmInstanceType(LastOperationJobContext context) {
        def instanceTypeParam = context.plan.parameters.find { it.name == PARAM_BOSH_VM_INSTANCE_TYPE }
        if (!instanceTypeParam) {
            throw new RuntimeException("Missing plan paramemter:${PARAM_BOSH_VM_INSTANCE_TYPE}")
        }
        return instanceTypeParam.value
    }

    Collection<ServiceDetail> handleTemplatingAndCreateDeployment(ProvisionRequest provisionRequest, BoshTemplateCustomizer templateCustomizer) {
        BoshTemplate template = boshTemplateFactory.build(readTemplate(provisionRequest.plan.templateUniqueIdentifier))
        template.replace('guid', provisionRequest.serviceInstanceGuid)
        template.replace(PARAM_PREFIX, DEPLOYMENT_PREFIX)
        template.replace(PARAM_BOSH_DIRECTOR_UUID, createBoshClient().fetchBoshInfo().uuid)

        updateTemplateFromDatabaseConfiguration(template, provisionRequest)
        def serviceDetails = templateCustomizer.customizeBoshTemplate(template, provisionRequest)

        if (!serviceDetails) {
            serviceDetails = []
        }
        serviceDetails.add(ServiceDetail.from(ServiceDetailKey.BOSH_DEPLOYMENT_ID, generateDeploymentId(provisionRequest.serviceInstanceGuid)))
        serviceDetails.add(ServiceDetail.from(ServiceDetailKey.BOSH_TASK_ID_FOR_DEPLOY, createBoshClient().postDeployment(template.build())))

        generateHostNames(provisionRequest.serviceInstanceGuid, template.instanceCount()).each {
            serviceDetails.add(ServiceDetail.from(ServiceDetailKey.HOST, it))
        }

        return serviceDetails
    }

    @VisibleForTesting
    private List<String> generateHostNames(String guid, int hostCount) {
        (List<String>) (0..<hostCount).inject([]) {
            result, i -> result.add("${guid}-${i}${HOST_NAME_POSTFIX}"); return result
        }
    }

    @VisibleForTesting
    private String readTemplate(String templateIdentifier) {
        Preconditions.checkNotNull(templateIdentifier, 'Need a valid template name!')
        File file = new File(serviceConfig.boshManifestFolder, templateIdentifier + (templateIdentifier.endsWith('.yml') ? '' : '.yml'))
        log.info("Using template file:${file.absolutePath}")
        return file.text
    }

    private static String generateDeploymentId(String serviceInstanceGuid) {
        return DEPLOYMENT_PREFIX + serviceInstanceGuid
    }

    private
    static void updateTemplateFromDatabaseConfiguration(BoshTemplate template, ProvisionRequest provisionRequest) {
        provisionRequest.plan.parameters?.each { Parameter p -> template.replace(p.name, p.value) }
    }

    boolean isBoshDeployTaskSuccessful(LastOperationJobContext context) {
        return isBoshTaskSuccessful(findBoshTaskIdForDeploy(context))
    }

    boolean isBoshTaskSuccessful(String taskId) {
        def task = createBoshClient().getTask(taskId)
        if (task.state == null) {
            throw new RuntimeException("Unknown bosh task state:${task.toString()}")
        }
        if ([TaskDto.State.cancelled, TaskDto.State.cancelling, TaskDto.State.errored].contains(task.state)) {
            throw new RuntimeException("Task failed: ${task.toString()}")
        }
        return TaskDto.State.done == task.state
    }

    private BoshClient createBoshClient() {
        return boshClientFactory.build(serviceConfig)
    }

    private static String findBoshDeploymentId(LastOperationJobContext context) {
        return ServiceDetailsHelper.from(context.serviceInstance.details).findValue(ServiceDetailKey.BOSH_DEPLOYMENT_ID).or(generateDeploymentId(context.serviceInstance.guid))
    }

    Optional<String> deleteBoshDeploymentIfExists(LastOperationJobContext lastOperationJobContext) {
        return deleteBoshDeployment(findBoshDeploymentId(lastOperationJobContext))
    }

    private Optional<String> deleteBoshDeployment(String id) {
        return createBoshClient().deleteDeploymentIfExists(id)
    }

    void removeVmInBoshCloudConfig(LastOperationJobContext context) {
        createBoshClient().removeVmInCloudConfig(context.deprovisionRequest.serviceInstanceGuid)
    }

    void deleteOpenStackServerGroup(String serverGroupId) {
        createOpenstackClient().deleteServerGroup(serverGroupId)
    }

    void deleteOpenStackServerGroupIfExists(LastOperationJobContext context) {
        def maybeServerGroupId = findServerGroupId(context)
        if (maybeServerGroupId.present) {
            deleteOpenStackServerGroup(maybeServerGroupId.get())
        }
    }

    BoshClientFactory getBoshClientFactory() {
        return boshClientFactory
    }

    OpenStackClientFactory getOpenStackClientFactory() {
        return openStackClientFactory
    }

    BoshBasedServiceConfig getServiceConfig() {
        return serviceConfig
    }

    BoshTemplateFactory getBoshTemplateFactory() {
        return boshTemplateFactory
    }

    boolean isBoshUndeployTaskSuccessful(LastOperationJobContext lastOperationJobContext) {
        Optional<String> maybe =  ServiceDetailsHelper.from(lastOperationJobContext.serviceInstance.details).findValue(ServiceDetailKey.BOSH_TASK_ID_FOR_UNDEPLOY)
        if(maybe.present){
            return isBoshTaskSuccessful(maybe.get())
        }else{
            return true
        }
    }
}
