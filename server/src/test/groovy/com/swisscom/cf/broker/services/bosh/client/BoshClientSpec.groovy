package com.swisscom.cf.broker.services.bosh.client

import com.swisscom.cf.broker.services.bosh.BoshConfig
import com.swisscom.cf.broker.util.MutexFactory
import groovy.json.JsonSlurper
import org.springframework.http.HttpStatus
import org.yaml.snakeyaml.Yaml
import spock.lang.Specification
import com.swisscom.cf.broker.util.Resource

class BoshClientSpec extends Specification {
    private BoshClient client


    def setup(){
        def boshRestClient = Mock(BoshRestClient)
        boshRestClient.boshConfig >> Stub(BoshConfig)
        def mutexFactory = Mock(MutexFactory)
        client = new BoshClient(boshRestClient,mutexFactory)
    }

    def "happy path: FetchCloudConfig"() {
        given:
        1 * client.boshRestClient.fetchCloudConfig() >> Resource.readTestFileContent("/bosh/cloud_config.json")
        when:
        def result = client.fetchCloudConfig()
        then:
        result.created_at
        result.properties
    }

    def "happy path:UpdateCloudConfig"() {
        given:
        def cloudConfigData = "SomeData"
        1 * client.mutexFactory.getNamedMutex(_) >> new Object()
        when:
        client.updateCloudConfig(cloudConfigData)
        then:
        1 * client.boshRestClient.postCloudConfig(cloudConfigData)
    }

    def "AddOrUpdateVmInCloudConfig"() {
        given:
        def vm = 'VM_NEW_FOR_TEST'
        def instanceType = 'instanceType'
        def affinityGroup = 'affinityGroup'
        and:
        client.mutexFactory.getNamedMutex(_) >> new Object()
        and:
        1 * client.boshRestClient.fetchCloudConfig() >> Resource.readTestFileContent("/bosh/cloud_config.json")
        and:
        def expectedInput = Resource.readTestFileContent("/bosh/cloud_config_with_test_vm.yml")


        when:
        client.addOrUpdateVmInCloudConfig(vm, instanceType, affinityGroup)

        then:
        1 * client.boshRestClient.postCloudConfig(_)>> {String s ->
            def given = (Map) new Yaml().load(s)
            def expected = (Map) new Yaml().load(expectedInput)
            assert ((List)given['vm_types']).size() == ((List)expected['vm_types']).size()
            assert ((List)given['vm_types']).last().name == vm
        }
    }

    def "RemoveVmInCloudConfig"() {
        given:
        def vm = 'us2agejk5bmuya7s'
        and:
        client.mutexFactory.getNamedMutex(_) >> new Object()
        and:
        def initialCloudConfig = Resource.readTestFileContent("/bosh/cloud_config.json")
        1 * client.boshRestClient.fetchCloudConfig() >> initialCloudConfig


        when:
        client.removeVmInCloudConfig(vm)

        then:
        1 * client.boshRestClient.postCloudConfig(_)>> {String s ->
            def given = (Map) new Yaml().load(s)
            def initial = (Map) new Yaml().load(new JsonSlurper().parseText(initialCloudConfig).first().properties as String)
            assert ((List)given['vm_types']).size() == (((List)initial['vm_types']).size() - 1)
        }
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
}
