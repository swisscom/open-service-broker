package com.swisscom.cf.broker.services.bosh

import com.google.common.annotations.VisibleForTesting
import com.google.common.base.Optional
import com.google.common.base.Preconditions
import com.swisscom.cf.broker.async.lastoperation.LastOperationJobContext
import com.swisscom.cf.broker.model.Parameter
import com.swisscom.cf.broker.model.ProvisionRequest
import com.swisscom.cf.broker.model.ServiceDetail
import com.swisscom.cf.broker.openstack.OpenStackClient
import com.swisscom.cf.broker.openstack.OpenStackClientFactory
import com.swisscom.cf.broker.services.bosh.client.BoshClient
import com.swisscom.cf.broker.services.bosh.client.BoshClientFactory
import com.swisscom.cf.broker.services.bosh.dto.TaskDto
import com.swisscom.cf.broker.services.common.async.AsyncOperationResult
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

    Optional<AsyncOperationResult> handleBoshProvisioning(LastOperationJobContext context, BoshTemplateCustomizer templateCustomizer) {
        def optionalProvisionState = getProvisionState(context)
        if (!optionalProvisionState.present) {
            return Optional.absent()
        }

        def provisionState = optionalProvisionState.get()
        Collection<ServiceDetail> details = []
        if (BoshProvisionState.BOSH_INITIAL == provisionState) {
            String serverGroupId = createOpenStackServerGroup(context.provisionRequest.serviceInstanceGuid)
            details.add(ServiceDetail.from(ServiceDetailKey.CLOUD_PROVIDER_SERVER_GROUP_ID, serverGroupId))
            provisionState = BoshProvisionState.CLOUD_PROVIDER_SERVER_GROUP_CREATED
        } else if (BoshProvisionState.CLOUD_PROVIDER_SERVER_GROUP_CREATED == provisionState) {
            addOrUpdateVmInBoshCloudConfig(context)
            provisionState = BoshProvisionState.BOSH_CLOUD_CONFIG_UPDATED
        } else if (BoshProvisionState.BOSH_CLOUD_CONFIG_UPDATED == provisionState) {
            details = handleTemplatingAndCreateDeployment(context.provisionRequest, templateCustomizer)
            provisionState = BoshProvisionState.BOSH_DEPLOYMENT_TRIGGERED
        } else if (BoshProvisionState.BOSH_DEPLOYMENT_TRIGGERED == provisionState) {
            if (isBoshTaskSuccessful(findBoshTaskIdForDeploy(context))) {
                provisionState = BoshProvisionState.BOSH_TASK_SUCCESSFULLY_FINISHED
            }
        }
        return Optional.of(new AsyncOperationResult(status: provisionState.status, internalStatus: provisionState.name(), details: details))
    }

    private Optional<BoshProvisionState> getProvisionState(LastOperationJobContext context) {
        Optional<BoshProvisionState> provisionState = null
        if (!context.lastOperation.internalState) {
            provisionState = Optional.of(BoshProvisionState.BOSH_INITIAL)
        } else {
            provisionState = BoshProvisionState.of(context.lastOperation.internalState)
        }
        return provisionState
    }

    private String createOpenStackServerGroup(String name) {
        return createOpenstackClient().createAntiAffinityServerGroup(name).id
    }

    private OpenStackClient createOpenstackClient() {
        return openStackClientFactory.createOpenStackClient(serviceConfig.openstackkUrl,
                serviceConfig.openstackUsername, serviceConfig.openstackPassword, serviceConfig.openstackTenantName)
    }

    private def addOrUpdateVmInBoshCloudConfig(LastOperationJobContext context) {
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

    private Collection<ServiceDetail> handleTemplatingAndCreateDeployment(ProvisionRequest provisionRequest, BoshTemplateCustomizer templateCustomizer) {
        BoshTemplate template = boshTemplateFactory.build(readTemplate(provisionRequest.plan.templateUniqueIdentifier))
        template.replace('guid', provisionRequest.serviceInstanceGuid)
        template.replace(PARAM_PREFIX, DEPLOYMENT_PREFIX)
        template.replace(PARAM_BOSH_DIRECTOR_UUID, createBoshClient().fetchBoshInfo().uuid)

        updateTemplateFromDatabaseConfiguration(template, provisionRequest)
        def serviceDetails = templateCustomizer.customize(template, provisionRequest)

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

    private boolean isBoshTaskSuccessful(String taskId) {
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

    Optional<AsyncOperationResult> handleBoshDeprovisioning(LastOperationJobContext context) {
        Optional<BoshDeprovisionState> optionalDeprovisionState = getDeprovisionState(context)
        if (!optionalDeprovisionState.present) {
            return Optional.absent()
        }
        def deprovisionState = optionalDeprovisionState.get()
        Collection<ServiceDetail> details = []
        if (BoshDeprovisionState.BOSH_INITIAL == deprovisionState) {
            Optional<String> optionalTaskId = deleteBoshDeploymentIfExists(findBoshDeploymentId(context))
            if (optionalTaskId.present) {
                details.add(ServiceDetail.from(ServiceDetailKey.BOSH_TASK_ID_FOR_UNDEPLOY, optionalTaskId.get()))
                deprovisionState = BoshDeprovisionState.BOSH_DEPLOYMENT_DELETION_REQUESTED
            } else {
                deprovisionState = BoshDeprovisionState.BOSH_TASK_SUCCESSFULLY_FINISHED
            }
        } else if (BoshDeprovisionState.BOSH_DEPLOYMENT_DELETION_REQUESTED == deprovisionState) {
            if (isBoshTaskSuccessful(findBoshTaskIdForUndeploy(context))) {
                deprovisionState = BoshDeprovisionState.BOSH_TASK_SUCCESSFULLY_FINISHED
            }
        } else if (BoshDeprovisionState.BOSH_TASK_SUCCESSFULLY_FINISHED == deprovisionState) {
            removeVmInBoshCloudConfig(context)
            deprovisionState = BoshDeprovisionState.BOSH_CLOUD_CONFIG_UPDATED
        } else if (BoshDeprovisionState.BOSH_CLOUD_CONFIG_UPDATED == deprovisionState) {
            def maybeServerGroupId = findServerGroupId(context)
            if (maybeServerGroupId.present) {
                deleteOpenStackServerGroup(maybeServerGroupId.get())
            }
            deprovisionState = BoshDeprovisionState.CLOUD_PROVIDER_SERVER_GROUP_DELETED
        }
        return Optional.of(new AsyncOperationResult(status: deprovisionState.status,
                internalStatus: deprovisionState.name(), details: details))
    }

    private static Optional<BoshDeprovisionState> getDeprovisionState(LastOperationJobContext context) {
        Optional<BoshDeprovisionState> state = null
        if (!context.lastOperation.internalState) {
            state = Optional.of(BoshDeprovisionState.BOSH_INITIAL)
        } else {
            state = BoshDeprovisionState.of(context.lastOperation.internalState)
        }
        return state
    }

    private static String findBoshDeploymentId(LastOperationJobContext context) {
        return ServiceDetailsHelper.from(context.serviceInstance.details).findValue(ServiceDetailKey.BOSH_DEPLOYMENT_ID).or(generateDeploymentId(context.serviceInstance.guid))
    }

    private Optional<String> deleteBoshDeploymentIfExists(String id) {
        return createBoshClient().deleteDeploymentIfExists(id)
    }

    private void removeVmInBoshCloudConfig(LastOperationJobContext context) {
        createBoshClient().removeVmInCloudConfig(context.deprovisionRequest.serviceInstanceGuid)
    }

    private String deleteOpenStackServerGroup(String serverGroupId) {
        return createOpenstackClient().deleteServerGroup(serverGroupId)
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
