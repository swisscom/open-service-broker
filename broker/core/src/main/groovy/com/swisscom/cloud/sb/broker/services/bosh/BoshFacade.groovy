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

package com.swisscom.cloud.sb.broker.services.bosh

import com.google.common.annotations.VisibleForTesting
import com.google.common.base.Optional
import com.google.common.base.Preconditions
import com.swisscom.cloud.sb.broker.model.Parameter
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.services.bosh.client.BoshClient
import com.swisscom.cloud.sb.broker.services.bosh.client.BoshClientFactory
import com.swisscom.cloud.sb.broker.services.bosh.dto.BoshConfigRequestDto
import com.swisscom.cloud.sb.broker.services.bosh.dto.BoshConfigResponseDto
import com.swisscom.cloud.sb.broker.services.bosh.dto.TaskDto
import com.swisscom.cloud.sb.broker.services.common.TemplateConfig
import com.swisscom.cloud.sb.broker.util.Resource
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailKey
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@CompileStatic
@Slf4j
class BoshFacade {
    public static final String DEPLOYMENT_PREFIX = 'd-'
    public static final String HOST_NAME_POSTFIX = '.service.consul'
    public static final String PARAM_BOSH_DIRECTOR_UUID = "bosh-director-uuid"
    public static final String PARAM_PREFIX = 'prefix'
    public static final String PARAM_GUID = 'guid'

    private final BoshClientFactory boshClientFactory
    private final BoshBasedServiceConfig serviceConfig
    private final BoshTemplateFactory boshTemplateFactory
    private final TemplateConfig templateConfig

    BoshFacade(BoshClientFactory boshClientFactory, BoshBasedServiceConfig serviceConfig, BoshTemplateFactory boshTemplateFactory, TemplateConfig templateConfig) {
        this.boshClientFactory = boshClientFactory
        this.serviceConfig = serviceConfig
        this.boshTemplateFactory = boshTemplateFactory
        this.templateConfig = templateConfig
    }

    private static String findBoshTaskIdForDeploy(LastOperationJobContext context) {
        return ServiceDetailsHelper.from(context.serviceInstance.details).getValue(BoshServiceDetailKey.BOSH_TASK_ID_FOR_DEPLOY)
    }

    Collection<ServiceDetail> handleTemplatingAndCreateDeployment(ProvisionRequest provisionRequest, BoshTemplateCustomizer templateCustomizer) {

        BoshTemplate template = boshTemplateFactory.build(readTemplateContent(provisionRequest.plan.templateUniqueIdentifier))

        if (serviceConfig.shuffleAzs) {
            template.shuffleAzs()
        }

        template.replace(PARAM_GUID, provisionRequest.serviceInstanceGuid)
        template.replace(PARAM_PREFIX, DEPLOYMENT_PREFIX)
        template.replace(PARAM_BOSH_DIRECTOR_UUID, createBoshClient().fetchBoshInfo().uuid)

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

    void handleTemplatingAndCreateConfigs(ProvisionRequest provisionRequest, BoshTemplateCustomizer templateCustomizer) {
        for (config in serviceConfig.boshConfigs) {
            BoshTemplate template = boshTemplateFactory.build(templateConfig.getTemplateForServiceKey(config.get('templateName')).first())
            template.replace(PARAM_GUID, provisionRequest.serviceInstanceGuid)
            templateCustomizer.customizeBoshConfigTemplate(template, config.get('type'), provisionRequest)
            createBoshClient().setConfig(new BoshConfigRequestDto(name: provisionRequest.serviceInstanceGuid, type: config.get('type'), content: template.build()))
        }
    }

    @VisibleForTesting
    private List<String> generateHostNames(String guid, int hostCount) {
        (List<String>) (0..<hostCount).inject([]) {
            result, i -> result.add("${guid}-${i}${HOST_NAME_POSTFIX}"); return result
        }
    }

    private String readTemplateContent(String templateIdentifier, String version = "1.0.0") {
        Preconditions.checkNotNull(templateIdentifier, 'Need a valid template name!')
        try {
            String template = templateConfig.getTemplateForServiceKey(templateIdentifier, version)[0]
            return template
        } catch (NoSuchElementException e) {
            // Fallback method which was used by BOSH deployments
            String fileName = templateIdentifier + (templateIdentifier.endsWith('.yml') ? '' : '.yml')
            File file = new File(serviceConfig.boshManifestFolder, fileName)
            if (file.exists()) {
                log.info("Using template file:${file.absolutePath}")
                return file.text
            }
            log.info("Will try to read file:${fileName} from embedded resources")
            return Resource.readTestFileContent(fileName.startsWith('/') ? fileName : ('/' + fileName))
        }
    }

    @VisibleForTesting
    static String generateDeploymentId(String serviceInstanceGuid) {
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

    void deleteBoshConfigs(LastOperationJobContext lastOperationJobContext) {
        deleteConfig(lastOperationJobContext.serviceInstance.guid, null)
    }

    Optional<String> deleteBoshDeploymentIfExists(LastOperationJobContext lastOperationJobContext) {
        return deleteBoshDeployment(findBoshDeploymentId(lastOperationJobContext))
    }

    private Optional<String> deleteBoshDeployment(String id) {
        return createBoshClient().deleteDeploymentIfExists(id)
    }

    boolean isBoshUndeployTaskSuccessful(LastOperationJobContext lastOperationJobContext) {
        Optional<String> maybe =  ServiceDetailsHelper.from(lastOperationJobContext.serviceInstance.details).findValue(BoshServiceDetailKey.BOSH_TASK_ID_FOR_UNDEPLOY)
        if(maybe.present){
            return isBoshTaskSuccessful(maybe.get())
        }else{
            return true
        }
    }

    void setConfig(String name, String type, String content) {
        createBoshClient().setConfig(new BoshConfigRequestDto(name: name, type: type, content: content))
    }

    void deleteConfig(String name, String type) {
        createBoshClient().deleteConfigIfExists(name, type)
    }

    List<BoshConfigResponseDto> getConfigs(String name, String type) {
        createBoshClient().getConfigs(name, type)
    }

    BoshClientFactory getBoshClientFactory() {
        return boshClientFactory
    }

    BoshBasedServiceConfig getServiceConfig() {
        return serviceConfig
    }

    BoshTemplateFactory getBoshTemplateFactory() {
        return boshTemplateFactory
    }

}
