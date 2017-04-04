package com.swisscom.cf.broker.com.swisscom.cf.broker.services.ecs.facade.client.details

import com.swisscom.cf.broker.BaseTransactionalSpecification
import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.facade.client.details.TokenManager
import com.swisscom.cf.broker.services.ecs.facade.client.details.exceptions.ECSAuthenticationException
import com.swisscom.cf.broker.util.RestTemplateFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders

class TokenManagerSpec extends BaseTransactionalSpecification {

    @Autowired
    ECSConfig ecsConfig

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
        thrown(ECSAuthenticationException)
    }

    def "request with expired X-SDS-AUTH-TOKEN"() {
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

}
