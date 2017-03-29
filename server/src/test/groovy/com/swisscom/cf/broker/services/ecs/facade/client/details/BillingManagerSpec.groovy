package com.swisscom.cf.broker.services.ecs.facade.client.details

import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtBillingInformationResponse
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespacePayload
import com.swisscom.cf.broker.services.ecs.facade.client.rest.RestTemplateReLoginDecorated
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

class BillingManagerSpec extends Specification {

    BillingManager billingManager
    RestTemplateReLoginDecorated restTemplateFactoryReLoginDecorated
    ECSMgmtNamespacePayload ecsMgmtNamespacePayload
    ECSConfig ecsConfig

    def setup() {
        ecsMgmtNamespacePayload = new ECSMgmtNamespacePayload(namespace: "namespace")
        restTemplateFactoryReLoginDecorated = Stub()
        ecsConfig = Stub()
        billingManager = new BillingManager(ecsConfig)
        billingManager.restTemplateReLoginDecorated = restTemplateFactoryReLoginDecorated
    }

    def "get billing information calls proper endpoint"() {
        when:
        ECSMgmtBillingInformationResponse ecsMgmtBillingInformationResponse = new ECSMgmtBillingInformationResponse()
        ResponseEntity result = new ResponseEntity(ecsMgmtBillingInformationResponse, HttpStatus.ACCEPTED)
        ecsConfig.getEcsManagementBaseUrl() >> "http.server.com"
        restTemplateFactoryReLoginDecorated.exchange("http.server.com/object/billing/namespace/namespace/info?sizeunit=KB", HttpMethod.GET, _, _) >> result
        then:
        billingManager.getInformation(ecsMgmtNamespacePayload) == ecsMgmtBillingInformationResponse
    }


    def "get billing information returns proper data"() {
        when:
        ECSMgmtBillingInformationResponse ecsMgmtBillingInformationResponse = new ECSMgmtBillingInformationResponse()
        ResponseEntity result = new ResponseEntity(ecsMgmtBillingInformationResponse, HttpStatus.ACCEPTED)
        ecsConfig.getEcsManagementBaseUrl() >> "http.server.com"
        restTemplateFactoryReLoginDecorated.exchange("http.server.com/object/billing/namespace/namespace/info?sizeunit=KB", HttpMethod.GET, _, _) >> result
        then:
        billingManager.getInformation(ecsMgmtNamespacePayload) == ecsMgmtBillingInformationResponse
    }

}
