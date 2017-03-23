package com.swisscom.cf.broker.functional

import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.domain.Namespace
import com.swisscom.cf.broker.services.ecs.facade.client.details.NamespaceManager
import com.swisscom.cf.broker.services.ecs.facade.client.details.TokenManager
import com.swisscom.cf.broker.services.ecs.facade.client.details.UserManager
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespacePayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserPayload
import com.swisscom.cf.broker.services.ecs.facade.client.rest.RestTemplateFactoryReLoginDecorated
import com.swisscom.cf.broker.util.RestTemplateFactory
import com.swisscom.cf.broker.util.ServiceLifeCycler
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class ECSFlowFunctionalSpec extends BaseFunctionalSpec {


    @Override
    void init(ServiceLifeCycler serviceLifeCycler) {
        super.init(serviceLifeCycler)
    }

    def "list namespaces"() {
        given:
        RestTemplateFactory restTemplate = new RestTemplateFactory()
        TokenManager tokenManager = Stub()

        HttpHeaders httpHeaders = new HttpHeaders()
        LinkedList<String> list = new LinkedList()
        list.add("BAAcdisyZVpkd0VGd25vOUc4b2Z4ZDNmdk9CcU5ZPQMAjAQASHVybjpzdG9yYWdlb3M6VmlydHVhbERhdGFDZW50ZXJEYXRhOjBlYWVkNzVmLTJlMWMtNDVmMC04MWIxLTAzOTAwZjEyYzZjYgIADTE0OTAxMzEwMDIyOTUDAC51cm46VG9rZW46NzI3OTUzZTAtMmMxOC00MGVjLTg2OWYtZTQ4MWE1YmY5NmZmAgAC0A8=")
        httpHeaders.put("X-SDS-AUTH-TOKEN", list)

        LinkedList<String> list2 = new LinkedList()
        list2.add("application/json")
        httpHeaders.put("Content-Type", list2)
        tokenManager.getHeaders() >> httpHeaders

        ECSConfig ecsConfig = Stub()
        ecsConfig.getEcsManagementBaseUrl() >> "https://ds11mgmt.swisscom.com:8443"
        RestTemplateFactoryReLoginDecorated restTemplateFactoryReLoginDecorated = new RestTemplateFactoryReLoginDecorated(restTemplate, tokenManager)
        NamespaceManager namespaceManager = new NamespaceManager(restTemplateFactoryReLoginDecorated: restTemplateFactoryReLoginDecorated, ecsConfig: ecsConfig)
        when:
        ResponseEntity response = namespaceManager.list()
        //def result = (new JsonSlurper()).parseText(response.getBody())
        then:
        response.statusCode == HttpStatus.OK

    }

    def "create/delete namespaces"() {
        given:
        RestTemplateFactory restTemplate = new RestTemplateFactory()
        TokenManager tokenManager = Stub()

        HttpHeaders httpHeaders = new HttpHeaders()
        LinkedList<String> list = new LinkedList()
        list.add("CODE")
        httpHeaders.put("X-SDS-AUTH-TOKEN", list)

        LinkedList<String> list2 = new LinkedList()
        list2.add("application/json")
        httpHeaders.put("Content-Type", list2)
        tokenManager.getHeaders() >> httpHeaders

        ECSConfig ecsConfig = Stub()
        ecsConfig.getEcsManagementBaseUrl() >> "https://ds11mgmt.swisscom.com:8443"
        RestTemplateFactoryReLoginDecorated restTemplateFactoryReLoginDecorated = new RestTemplateFactoryReLoginDecorated(restTemplate, tokenManager)
        NamespaceManager namespaceManager = new NamespaceManager(restTemplateFactoryReLoginDecorated: restTemplateFactoryReLoginDecorated, ecsConfig: ecsConfig)

        ECSMgmtNamespacePayload namespace = new ECSMgmtNamespacePayload()
        namespace.namespace = "1fe5ba4816123317e943579636e88e29"
        namespace.default_data_services_vpool = "urn:storageos:ReplicationGroupInfo:e4cf1d55-7f6f-4e64-be95-52c87d3b465d:global"
        namespace.is_encryption_enabled = false
        namespace.default_bucket_block_size = -1
        namespace.is_stale_allowed = true
        namespace.compliance_enabled = false

        UserManager userManager = new UserManager(restTemplateFactoryReLoginDecorated: restTemplateFactoryReLoginDecorated, ecsConfig: ecsConfig)
        ECSMgmtUserPayload ecsMgmtUserPayload = new ECSMgmtUserPayload()
        ecsMgmtUserPayload.namespace = "1fe5ba4816123317e943579636e88e29"
        ecsMgmtUserPayload.user = "1fe5ba4816123317e943579636e88e29-user2"
        when:
        //ResponseEntity<String> response = namespaceManager.create(namespace)

        ResponseEntity<String> response = userManager.create(ecsMgmtUserPayload)
        System.out.println(response.getBody())
        System.out.println(response.getHeaders())
        //def result = (new JsonSlurper()).parseText(response.getBody())
        then:
        response.statusCode == HttpStatus.OK

    }

}