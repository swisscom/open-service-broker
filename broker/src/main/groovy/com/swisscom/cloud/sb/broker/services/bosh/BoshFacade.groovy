package com.swisscom.cloud.sb.broker.services.bosh

import com.google.common.annotations.VisibleForTesting
import com.google.common.base.Optional
import com.google.common.base.Preconditions
import com.swisscom.cloud.sb.broker.model.Parameter
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.services.bosh.client.BoshClient
import com.swisscom.cloud.sb.broker.services.bosh.client.BoshClientFactory
import com.swisscom.cloud.sb.broker.services.bosh.dto.TaskDto
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.openstack.OpenStackClient
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.openstack.OpenStackClientFactory
import com.swisscom.cloud.sb.broker.util.Resource
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailKey
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@CompileStatic
@Slf4j
class BoshFacade {
    public static final String PLAN_PARAMETER_BOSH_VM_INSTANCE_TYPE = 'vm_instance_type'
    public static final String PLAN_PARAMETER_CREATE_OPEN_STACK_SERVER_GROUP = 'create_openstack_server_group'

    public static final String DEPLOYMENT_PREFIX = 'd-'
    public static final String HOST_NAME_POSTFIX = '.service.consul'
    public static final String PARAM_BOSH_DIRECTOR_UUID = "bosh-director-uuid"
    public static final String PARAM_PREFIX = 'prefix'
    public static final String PARAM_GUID = 'guid'
    public static final String PARAM_VM_TYPE = 'vm-type'


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
        createBoshClient().addOrUpdateVmInCloudConfig(context.provisionRequest.serviceInstanceGuid, findBoshVmInstanceType(context.plan), findServerGroupId(context).get())
    }

    @VisibleForTesting
    private Optional<String> findServerGroupId(LastOperationJobContext context) {
        Optional<String> maybeGroupId = ServiceDetailsHelper.from(context.serviceInstance.details).findValue(BoshServiceDetailKey.CLOUD_PROVIDER_SERVER_GROUP_ID)
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
        return ServiceDetailsHelper.from(context.serviceInstance.details).getValue(BoshServiceDetailKey.BOSH_TASK_ID_FOR_DEPLOY)
    }

    private static String findBoshVmInstanceType(Plan plan) {
        def instanceTypeParam = plan.parameters.find { it.name == PLAN_PARAMETER_BOSH_VM_INSTANCE_TYPE }
        if (!instanceTypeParam) {
            throw new RuntimeException("Missing plan paramemter:${PLAN_PARAMETER_BOSH_VM_INSTANCE_TYPE}")
        }
        return instanceTypeParam.value
    }

    Collection<ServiceDetail> handleTemplatingAndCreateDeployment(ProvisionRequest provisionRequest, BoshTemplateCustomizer templateCustomizer) {
        BoshTemplate template = boshTemplateFactory.build(readTemplateContent(provisionRequest.plan.templateUniqueIdentifier))

        if (serviceConfig.shuffleAzs) {
            template.shuffleAzs()
        }

        template.replace(PARAM_GUID, provisionRequest.serviceInstanceGuid)
        template.replace(PARAM_PREFIX, DEPLOYMENT_PREFIX)
        template.replace(PARAM_BOSH_DIRECTOR_UUID, createBoshClient().fetchBoshInfo().uuid)
        template.replace(BoshFacade.PARAM_VM_TYPE, shouldCreateOpenStackServerGroup(provisionRequest.plan)?provisionRequest.serviceInstanceGuid:findBoshVmInstanceType(provisionRequest.plan))

        updateTemplateFromDatabaseConfiguration(template, provisionRequest)

        def serviceDetails = templateCustomizer.customizeBoshTemplate(template, provisionRequest)

        if (!serviceDetails) {
            serviceDetails = []
        }
        serviceDetails.add(ServiceDetail.from(BoshServiceDetailKey.BOSH_DEPLOYMENT_ID, generateDeploymentId(provisionRequest.serviceInstanceGuid)))
        serviceDetails.add(ServiceDetail.from(BoshServiceDetailKey.BOSH_TASK_ID_FOR_DEPLOY, createBoshClient().postDeployment(template.build())))

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

    private String readTemplateContent(String templateIdentifier) {
        Preconditions.checkNotNull(templateIdentifier, 'Need a valid template name!')
        String fileName = templateIdentifier + (templateIdentifier.endsWith('.yml') ? '' : '.yml')
        File file = new File(serviceConfig.boshManifestFolder, fileName)
        if(file.exists()){
            log.info("Using template file:${file.absolutePath}")
            return file.text
        }
        log.info("Will try to read file:${fileName} from embedded resources")
        return Resource.readTestFileContent(fileName.startsWith('/')?fileName:('/'+fileName))
    }

    @VisibleForTesting
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
        return ServiceDetailsHelper.from(context.serviceInstance.details).findValue(BoshServiceDetailKey.BOSH_DEPLOYMENT_ID).or(generateDeploymentId(context.serviceInstance.guid))
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

    void deleteOpenStackServerGroupIfExists(LastOperationJobContext context) {
        def maybeServerGroupId = findServerGroupId(context)
        if (maybeServerGroupId.present) {
            deleteOpenStackServerGroup(maybeServerGroupId.get())
        }
    }

    private void deleteOpenStackServerGroup(String serverGroupId) {
        createOpenstackClient().deleteServerGroup(serverGroupId)
    }

    boolean isBoshUndeployTaskSuccessful(LastOperationJobContext lastOperationJobContext) {
        Optional<String> maybe =  ServiceDetailsHelper.from(lastOperationJobContext.serviceInstance.details).findValue(BoshServiceDetailKey.BOSH_TASK_ID_FOR_UNDEPLOY)
        if(maybe.present){
            return isBoshTaskSuccessful(maybe.get())
        }else{
            return true
        }
    }

    boolean shouldCreateOpenStackServerGroup(LastOperationJobContext context) {
        return shouldCreateOpenStackServerGroup(context.plan)
    }

    boolean shouldCreateOpenStackServerGroup(Plan plan) {
        def param = plan?.parameters?.find { it.name == PLAN_PARAMETER_CREATE_OPEN_STACK_SERVER_GROUP }
        if(!param){
            return false
        }
        return param.value.toBoolean()
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

}
