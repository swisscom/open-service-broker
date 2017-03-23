package com.swisscom.cf.broker.functional

import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.facade.client.details.NamespaceManager
import com.swisscom.cf.broker.services.ecs.facade.client.details.SharedSecretKeyManager
import com.swisscom.cf.broker.services.ecs.facade.client.details.TokenManager
import com.swisscom.cf.broker.services.ecs.facade.client.details.UserManager
import com.swisscom.cf.broker.services.ecs.facade.client.details.exceptions.ECSAuthenticationProblemException
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespacePayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtSharedSecretKeyPayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserPayload
import com.swisscom.cf.broker.services.ecs.facade.client.rest.RestTemplateReLoginDecorated
import com.swisscom.cf.broker.util.RestTemplateFactory
import com.swisscom.cf.broker.util.ServiceLifeCycler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class ECSFlowFunctionalSpec extends BaseFunctionalSpec {

    @Autowired
    ECSConfig ecsConfig

    @Override
    void init(ServiceLifeCycler serviceLifeCycler) {
        super.init(serviceLifeCycler)
    }

    def "login with valid credentials"() {
        given:
        TokenManager tokenManager = new TokenManager(ecsConfig, new RestTemplateFactory())

        when:
        String xSDSAuthToken = tokenManager.refreshAuthToken().getHeaders().get("X-SDS-AUTH-TOKEN").get(0)

        then:
        xSDSAuthToken.isEmpty() == false
    }

    def "login with invalid credentials"() {
        given:
        ECSConfig badEcsConfig = ecsConfig.clone()
        badEcsConfig.ecsManagementUsername = badEcsConfig.ecsManagementUsername + "invalidUser" + new Random().nextInt(1000)
        TokenManager tokenManager = new TokenManager(badEcsConfig, new RestTemplateFactory())

        when:
        tokenManager.refreshAuthToken().getHeaders().get("X-SDS-AUTH-TOKEN").get(0)

        then:
        thrown(ECSAuthenticationProblemException)
    }

    def "request with experied X-SDS-AUTH-TOKEN"() {
        given:
        String expiredToken = "BAAcdisyZVpkd0VGd25vOUc4b2Z4ZDNmdk9CcU5ZPQMAjAQASHVybjpzdG9yYWdlb3M6VmlydHVhbERhdGFDZW50ZXJEYXRhOjBlYWVkNzVmLTJlMWMtNDVmMC04MWIxLTAzOTAwZjEyYzZjYgIADTE0OTAxMzEwMDIyOTUDAC51cm46VG9rZW46NzI3OTUzZTAtMmMxOC00MGVjLTg2OWYtZTQ4MWE1YmY5NmZmAgAC0A8="
        TokenManager tokenManager = new TokenManager(ecsConfig, new RestTemplateFactory())
        HttpHeaders httpHeaders = new HttpHeaders()
        LinkedList<String> list = new LinkedList()
        list.add(expiredToken)
        httpHeaders.put("X-SDS-AUTH-TOKEN", list)

        when:
        String xSDSAuthToken = tokenManager.refreshAuthToken().getHeaders().get("X-SDS-AUTH-TOKEN").get(0)

        then:
        expiredToken.length() == xSDSAuthToken.length()
        !expiredToken.equals(xSDSAuthToken)
    }

    def "list namespaces"() {
        given:
        TokenManager tokenManager = new TokenManager(ecsConfig, new RestTemplateFactory())
        RestTemplateReLoginDecorated restTemplateReLoginDecorated = new RestTemplateReLoginDecorated(tokenManager)
        NamespaceManager namespaceManager = new NamespaceManager(restTemplateReLoginDecorated: restTemplateReLoginDecorated, ecsConfig: ecsConfig)
        when:
        ResponseEntity response = namespaceManager.list()
        //def result = (new JsonSlurper()).parseText(response.getBody())
        then:
        response.statusCode == HttpStatus.OK

    }

    def "add remove secret key for user"() {
        given:
        RestTemplateFactory restTemplate = new RestTemplateFactory()
        TokenManager tokenManager = Stub()

        HttpHeaders httpHeaders = new HttpHeaders()
        LinkedList<String> list = new LinkedList()
        list.add("BAAcaExlMGI3RDRnaG0xQll6RGJRbnA5a2FCNHE4PQMAjAQASHVybjpzdG9yYWdlb3M6VmlydHVhbERhdGFDZW50ZXJEYXRhOjBlYWVkNzVmLTJlMWMtNDVmMC04MWIxLTAzOTAwZjEyYzZjYgIADTE0OTAyMTc0MDIzMjQDAC51cm46VG9rZW46NjE1ODFmNzUtMzEzNS00MTgzLTg1ZGMtZDUwZDQ2ODk0ZDg5AgAC0A8=")
        httpHeaders.put("X-SDS-AUTH-TOKEN", list)

        LinkedList<String> list2 = new LinkedList()
        list2.add("application/json")
        httpHeaders.put("Content-Type", list2)
        tokenManager.getHeaders() >> httpHeaders

        ECSConfig ecsConfig = Stub()
        ecsConfig.getEcsManagementBaseUrl() >> "https://ds11mgmt.swisscom.com:8443"
        RestTemplateReLoginDecorated restTemplateFactoryReLoginDecorated = new RestTemplateReLoginDecorated(restTemplate, tokenManager)
        SharedSecretKeyManager sharedSecretKeyManager = new SharedSecretKeyManager(restTemplateFactoryReLoginDecorated: restTemplateFactoryReLoginDecorated, ecsConfig: ecsConfig)

        ECSMgmtSharedSecretKeyPayload namespace = new ECSMgmtSharedSecretKeyPayload()
        namespace.namespace = "1fe5ba4816123317e943579636e88e29"
        ECSMgmtUserPayload user = new ECSMgmtUserPayload()
        user.user = "1fe5ba4816123317e943579636e88e29-user2"
        when:
        sharedSecretKeyManager.delete(user, namespace)
        ResponseEntity response = sharedSecretKeyManager.create(user, namespace)
        then:
        response.statusCode == HttpStatus.OK

    }

    def "add remove namespace"() {
        given:
        RestTemplateFactory restTemplate = new RestTemplateFactory()
        TokenManager tokenManager = Stub()

        HttpHeaders httpHeaders = new HttpHeaders()
        LinkedList<String> list = new LinkedList()
        list.add("BAAcb01udER6OXhBZmxUeUlQQW5MMVJ6M2xwSkZNPQMAjAQASHVybjpzdG9yYWdlb3M6VmlydHVhbERhdGFDZW50ZXJEYXRhOmFkYjU2OGIxLTZiMTYtNDE2OS04MWE4LWIzN2Q2ZGQxNDRmYQIADTE0OTAxMzA2OTE2NjIDAC51cm46VG9rZW46Nzk0MzA1NTEtNGNiNS00NjRlLTgyYjEtM2ZiNDQzNmJhNTAxAgAC0A8=")
        httpHeaders.put("X-SDS-AUTH-TOKEN", list)

        LinkedList<String> list2 = new LinkedList()
        list2.add("application/json")
        httpHeaders.put("Content-Type", list2)
        tokenManager.getHeaders() >> httpHeaders

        ECSConfig ecsConfig = Stub()
        ecsConfig.getEcsManagementBaseUrl() >> "https://ds11mgmt.swisscom.com:8443"
        RestTemplateReLoginDecorated restTemplateFactoryReLoginDecorated = new RestTemplateReLoginDecorated(restTemplate, tokenManager)
        NamespaceManager namespaceManager = new NamespaceManager(restTemplateFactoryReLoginDecorated: restTemplateFactoryReLoginDecorated, ecsConfig: ecsConfig)

        ECSMgmtNamespacePayload namespace = new ECSMgmtNamespacePayload()
        namespace.namespace = "1fe5ba4816123317e943579636e88e30"
        namespace.default_data_services_vpool = "urn:storageos:ReplicationGroupInfo:e4cf1d55-7f6f-4e64-be95-52c87d3b465d:global"
        namespace.is_encryption_enabled = false
        namespace.default_bucket_block_size = -1
        namespace.is_stale_allowed = true
        namespace.compliance_enabled = false


        when:
        ResponseEntity response = namespaceManager.create(namespace)
        namespaceManager.delete(namespace)
        then:
        response.statusCode == HttpStatus.OK

    }

    def "add remove user"() {
        given:
        RestTemplateFactory restTemplate = new RestTemplateFactory()
        TokenManager tokenManager = Stub()

        HttpHeaders httpHeaders = new HttpHeaders()
        LinkedList<String> list = new LinkedList()
        list.add("BAAcbGNmeUk2cXBjOTIvbGYyVnpUV3YwbWlFWDA0PQMAjAQASHVybjpzdG9yYWdlb3M6VmlydHVhbERhdGFDZW50ZXJEYXRhOjY3OGNhOTE2LTg5OTMtNGQzZC1hODg4LWUyYmJhNDZlOGZiZgIADTE0OTAyMTc1NjI4NDYDAC51cm46VG9rZW46NTZkNGE1ZmMtODk3My00MTkzLWE1YmEtNzQ0NDlhMjQ3N2NjAgAC0A8=")
        httpHeaders.put("X-SDS-AUTH-TOKEN", list)

        LinkedList<String> list2 = new LinkedList()
        list2.add("application/json")
        httpHeaders.put("Content-Type", list2)
        tokenManager.getHeaders() >> httpHeaders

        ECSConfig ecsConfig = Stub()
        ecsConfig.getEcsManagementBaseUrl() >> "https://ds11mgmt.swisscom.com:8443"
        RestTemplateReLoginDecorated restTemplateFactoryReLoginDecorated = new RestTemplateReLoginDecorated(restTemplate, tokenManager)
        UserManager userManager = new UserManager(restTemplateFactoryReLoginDecorated: restTemplateFactoryReLoginDecorated, ecsConfig: ecsConfig)

        ECSMgmtUserPayload ecsMgmtUserPayload = new ECSMgmtUserPayload()
        ecsMgmtUserPayload.namespace = "1fe5ba4816123317e943579636e88e29"
        ecsMgmtUserPayload.user = "1fe5ba4816123317e943579636e88e29-user4"

        when:
        userManager.create(ecsMgmtUserPayload)
        ResponseEntity response = userManager.delete(ecsMgmtUserPayload)
        then:
        response.statusCode == HttpStatus.OK

    }


}