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

package com.swisscom.cloud.sb.broker.services.bosh.client

import com.swisscom.cloud.sb.broker.services.bosh.BoshConfig
import com.swisscom.cloud.sb.broker.util.Resource
import org.springframework.http.HttpStatus
import spock.lang.Specification

class BoshClientSpec extends Specification {
    private BoshClient client


    def setup(){
        def boshRestClient = Mock(BoshRestClient)
        boshRestClient.boshConfig >> Stub(BoshConfig)
        client = new BoshClient(boshRestClient)
    }

    def "PostDeployment fails for non valid yml"() {
        when:
        client.postDeployment('')
        then:
        def ex = thrown(RuntimeException)
    }

    def "happy path:PostDeployment"() {
        given:
        def cloudConfig = Resource.readTestFileContent("/bosh/cloud_config.yml")
        when:
        client.postDeployment(cloudConfig)
        then:
        1 * client.boshRestClient.postDeployment(cloudConfig)
    }


    def "DeleteDeploymentIfExists handles exception when resource not found"() {
        given:
        def deploymentId = ''
        client.boshRestClient.deleteDeployment(deploymentId) >> {throw new BoshResourceNotFoundException('','','', HttpStatus.NOT_FOUND)}
        when:
        def result = client.deleteDeploymentIfExists(deploymentId)
        then:
        !result.present
    }

    def "happy path:DeleteDeploymentIfExists"() {
        given:
        def deploymentId = 'deploymentId'
        def taskId = 'taskId'
        client.boshRestClient.deleteDeployment(deploymentId) >> taskId
        when:
        def result = client.deleteDeploymentIfExists(deploymentId)
        then:
        result.get() == taskId
    }

    def "happy path: GetTask"() {
        given:
        def taskId = 'taskId'
        1 * client.boshRestClient.getTask(taskId) >> Resource.readTestFileContent('/bosh/task1.json')

        when:
        def result = client.getTask(taskId)

        then:
        result
    }

    def "happy path: fetchBoshInfo"() {
        given:
        1 * client.boshRestClient.fetchBoshInfo() >> Resource.readTestFileContent('/bosh/bosh_info.json')
        when:
        def result = client.fetchBoshInfo()
        then:
        result
    }

    def "happy path: GetAllVMsInDeployment"() {
        given:
        def deploymentId = 'deploymentId'
        1 * client.boshRestClient.getAllVMsInDeployment(deploymentId) >> Resource.readTestFileContent('/bosh/vms.json')

        when:
        def result = client.getAllVMsInDeployment(deploymentId)

        then:
        result
    }
}
