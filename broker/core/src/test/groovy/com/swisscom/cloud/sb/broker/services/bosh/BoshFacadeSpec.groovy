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

import com.google.common.base.Optional
import com.swisscom.cloud.sb.broker.model.*
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.services.bosh.client.BoshClient
import com.swisscom.cloud.sb.broker.services.bosh.client.BoshClientFactory
import com.swisscom.cloud.sb.broker.services.bosh.dto.BoshConfigResponseDto
import com.swisscom.cloud.sb.broker.services.bosh.dto.BoshInfoDto
import com.swisscom.cloud.sb.broker.services.bosh.dto.TaskDto
import com.swisscom.cloud.sb.broker.services.common.TemplateConfig
import spock.lang.Specification

class BoshFacadeSpec extends Specification {
    public static final String serviceInstanceGuid = "serviceInstanceGuid"

    BoshClient boshClient
    BoshClientFactory boshClientFactory
    BoshBasedServiceConfig serviceConfig
    BoshFacade boshFacade
    BoshTemplateFactory boshTemplateFactory
    BoshTemplate boshTemplate
    TemplateConfig templateConfig

    void setup() {
        boshClient = Mock(BoshClient)
        boshClientFactory = Mock(BoshClientFactory)
        boshClientFactory.build(*_) >> boshClient
        and:
        TemplateConfig.ServiceTemplate st = new TemplateConfig.ServiceTemplate(name: "test", version: "1.0.0", templates: [new File('src/test/resources/bosh/template_mongodbent_v5.yml').text])
        TemplateConfig.ServiceTemplate configST = new TemplateConfig.ServiceTemplate(name: "test-config", version: "1.0.0", templates: [new File('src/test/resources/bosh/cloud_config_template_mongodbent_v5.yml').text])
        GenericConfig genericConfig = new GenericConfig(templateName: 'test-config', type: 'cloud')
        serviceConfig = new DummyConfig(retryIntervalInSeconds: 1, maxRetryDurationInMinutes: 1, genericConfigs: [genericConfig])
        templateConfig = new TemplateConfig(serviceTemplates: [st, configST])
        and:
        boshTemplate = Mock(BoshTemplate)
        boshTemplateFactory = Mock(BoshTemplateFactory) { build(_) >> boshTemplate }
        and:
        boshFacade = new BoshFacade(boshClientFactory, serviceConfig, boshTemplateFactory, templateConfig)
    }

    def "host name creation works correctly"() {
        when:
        def result = boshFacade.generateHostNames('guid', 3)
        then:
        result.size() == 3
        result.contains("guid-0${BoshFacade.HOST_NAME_POSTFIX}")
    }

    def "templating from file is handled correctly"() {
        given:
        def request = new ProvisionRequest(serviceInstanceGuid: "guid", plan: new Plan(templateUniqueIdentifier: '/bosh/template_mongodbent_v5.yml',
                parameters: [new Parameter(name: 'name1', value: 'value1')]))
        def customizer = Mock(BoshTemplateCustomizer)
        and:
        def boshUuid = 'boshUuid'
        1 * boshClient.fetchBoshInfo() >> new BoshInfoDto(uuid: boshUuid)
        and:
        def serviceDetails = [new ServiceDetail()]
        1 * customizer.customizeBoshTemplate(boshTemplate, request) >> serviceDetails
        and:
        1 * boshClient.postDeployment(_) >> 'taskId'
        and:
        serviceConfig.shuffleAzs = true
        when:
        def result = boshFacade.handleTemplatingAndCreateDeployment(request, customizer)
        then:
        1 * boshTemplate.replace('guid', request.serviceInstanceGuid)
        1 * boshTemplate.replace('name1', 'value1')
        1 * boshTemplate.shuffleAzs()
        result.size() == 3
    }

    def "templating from config is handled correctly"() {
        given:
        def request = new ProvisionRequest(serviceInstanceGuid: "guid", plan: new Plan(templateUniqueIdentifier: 'test',
                parameters: [new Parameter(name: 'name1', value: 'value1')]))
        def customizer = Mock(BoshTemplateCustomizer)
        and:
        def boshUuid = 'boshUuid'
        1 * boshClient.fetchBoshInfo() >> new BoshInfoDto(uuid: boshUuid)
        and:
        def serviceDetails = [new ServiceDetail()]
        1 * customizer.customizeBoshTemplate(boshTemplate, request) >> serviceDetails
        and:
        1 * boshClient.postDeployment(_) >> 'taskId'
        and:
        serviceConfig.shuffleAzs = true
        when:
        def result = boshFacade.handleTemplatingAndCreateDeployment(request, customizer)
        then:
        1 * boshTemplate.replace('guid', request.serviceInstanceGuid)
        1 * boshTemplate.replace('name1', 'value1')
        1 * boshTemplate.shuffleAzs()
        result.size() == 3
    }

    def "templating generic config from config is handled correctly"() {
        given:
        def request = new ProvisionRequest(serviceInstanceGuid: "guid", plan: new Plan(templateUniqueIdentifier: 'test',
                parameters: [new Parameter(name: 'name1', value: 'value1')]))
        def customizer = Mock(BoshTemplateCustomizer)
        and:
        1 * customizer.customizeBoshConfigTemplate(boshTemplate, 'cloud', request) >> null
        and:
        1 * boshClient.setConfig(_) >> null
        when:
        boshFacade.handleTemplatingAndCreateConfigs(request, customizer)
        then:
        1 * boshTemplate.replace('guid', request.serviceInstanceGuid)
        noExceptionThrown()
    }

    def "bosh Deploy task state checking is handled correctly for normal cases"() {
        given:
        def taskId = 'taskId'
        def context = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: [ServiceDetail.from(BoshServiceDetailKey.BOSH_TASK_ID_FOR_DEPLOY, taskId)]))
        and:
        1 * boshClient.getTask(taskId) >> new TaskDto(state: boskTaskState)
        expect:
        boshFacade.isBoshDeployTaskSuccessful(context) == result

        where:
        result | boskTaskState
        true   | TaskDto.State.done
        false  | TaskDto.State.processing
    }

    def "bosh task state checking is handled correctly for normal cases"() {
        given:
        def taskId = 'taskId'
        and:
        1 * boshClient.getTask(taskId) >> new TaskDto(state: boskTaskState)
        expect:
        boshFacade.isBoshTaskSuccessful(taskId) == result

        where:
        result | boskTaskState
        true   | TaskDto.State.done
        false  | TaskDto.State.processing
    }

    def "bosh Deploy task state checking is handled correctly for error cases"() {
        given:
        def taskId = 'taskId'
        and:
        1 * boshClient.getTask(taskId) >> new TaskDto(state: boskTaskState)
        when:
        boshFacade.isBoshTaskSuccessful(taskId)
        then:
        def ex = thrown(RuntimeException)
        where:
        boskTaskState << [TaskDto.State.cancelled, TaskDto.State.cancelling, TaskDto.State.errored]
    }

    def "bosh Deploy task state checking is handled correctly for error state can not be parsed"() {
        given:
        def taskId = 'taskId'
        and:
        1 * boshClient.getTask(taskId) >> new TaskDto(state: null)
        when:
        boshFacade.isBoshTaskSuccessful(taskId)
        then:
        def ex = thrown(RuntimeException)
    }

    def "bosh UNDeploy task state checking is handled correctly for normal cases"() {
        given:
        def taskId = 'taskId'
        def context = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: [ServiceDetail.from(BoshServiceDetailKey.BOSH_TASK_ID_FOR_UNDEPLOY, taskId)]))
        and:
        1 * boshClient.getTask(taskId) >> new TaskDto(state: boskTaskState)
        expect:
        boshFacade.isBoshUndeployTaskSuccessful(context) == result

        where:
        result | boskTaskState
        true   | TaskDto.State.done
        false  | TaskDto.State.processing
    }

    def "deleting bosh deployment works correctly when bosh deployment id can be found in service details"() {
        given:
        Optional<String> taskId = Optional.absent()
        def deploymentId = "deploymentId"
        def context = new LastOperationJobContext(serviceInstance: new ServiceInstance(details: [ServiceDetail.from(BoshServiceDetailKey.BOSH_DEPLOYMENT_ID, deploymentId)]))
        1 * boshClient.deleteDeploymentIfExists(deploymentId) >> taskId

        expect:
        boshFacade.deleteBoshDeploymentIfExists(context) == taskId
    }

    def "deleting bosh deployment works correctly when bosh deployment id is *NOT*found in service details"() {
        given:
        Optional<String> taskId = Optional.absent()
        String serviceInstanceGuid = 'serviceInstanceGuid'
        def deploymentId = boshFacade.generateDeploymentId(serviceInstanceGuid)
        def context = new LastOperationJobContext(serviceInstance: new ServiceInstance(guid: serviceInstanceGuid))
        1 * boshClient.deleteDeploymentIfExists(deploymentId) >> taskId

        expect:
        boshFacade.deleteBoshDeploymentIfExists(context) == taskId
    }

    def "set cloud config"() {
        when:
        boshFacade.setConfig('test', 'cloud', '--- {}')

        then:
        1 * boshClient.setConfig(_)
    }

    def "get cloud config"() {
        given:
        1 * boshClient.getConfigs('test', 'cloud') >> [new BoshConfigResponseDto(name: 'test', type: 'cloud', content: '--- {}')]

        when:
        List<BoshConfigResponseDto> configs = boshFacade.getConfigs('test', 'cloud')

        then:
        configs.size() == 1
        configs[0].name == 'test'
    }

    def "delete cloud config"() {
        when:
        boshFacade.deleteConfig('test', 'cloud')

        then:
        1 * boshClient.deleteConfig('test', 'cloud')
    }

}
