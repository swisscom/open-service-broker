package com.swisscom.cf.broker.services.ecs.facade.client.details

import com.google.common.annotations.VisibleForTesting
import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtBillingInformationResponse
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespacePayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtSharedSecretKeyPayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtSharedSecretKeyResponse
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserPayload
import com.swisscom.cf.broker.services.ecs.facade.client.rest.RestTemplateReLoginDecorated
import com.swisscom.cf.broker.util.RestTemplateFactory
import groovy.transform.CompileStatic
import org.springframework.http.HttpMethod

@CompileStatic
class BillingManager {

    private final static String BILLING_NAMESPACE_PREFIX = "/object/billing/namespace/"
    private final static String BILLING_NAMESPACE_SUFIX = "/info?sizeunit=KB"

    @VisibleForTesting
    private RestTemplateReLoginDecorated<ECSMgmtNamespacePayload, ECSMgmtBillingInformationResponse> restTemplateReLoginDecorated
    @VisibleForTesting
    private ECSConfig ecsConfig

    BillingManager(ECSConfig ecsConfig) {
        this.ecsConfig = ecsConfig
        this.restTemplateReLoginDecorated = new RestTemplateReLoginDecorated<>(new TokenManager(ecsConfig, new RestTemplateFactory()))
    }

    ECSMgmtBillingInformationResponse getInformation(ECSMgmtNamespacePayload namespacePayload) {
        return restTemplateReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + BILLING_NAMESPACE_PREFIX + namespacePayload.getNamespace() + BILLING_NAMESPACE_SUFIX, HttpMethod.GET, null, ECSMgmtBillingInformationResponse.class).getBody()
    }

}
