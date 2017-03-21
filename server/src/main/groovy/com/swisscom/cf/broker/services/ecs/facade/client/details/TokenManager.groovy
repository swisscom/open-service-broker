package com.swisscom.cf.broker.services.ecs.facade.client.details

import com.google.common.annotations.VisibleForTesting
import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.facade.client.details.exceptions.ECSAuthenticationProblemException
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate

class TokenManager {

    @VisibleForTesting
    private static String X_SDS_AUTH_TOKEN = "STARTUP_DEFAULT_TOKEN"
    private static String TOKEN_NAME = "X-SDS-AUTH-TOKEN"
    @VisibleForTesting
    RestTemplate restTemplate
    @VisibleForTesting
    ECSConfig ecsConfig

    HttpHeaders getHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders()
        httpHeaders.put(TOKEN_NAME, getHeaderValueList(X_SDS_AUTH_TOKEN))
        return httpHeaders
    }

    def getHeaderValueList(String param) {
        LinkedList result = new LinkedList<String>()
        result.add(param)
        return result
    }

    def refreshHeaders() {
        List<String> result = restTemplate.exchange(ecsConfig.getEcsManagementBaseUrl(), HttpMethod.GET, new HttpEntity(null, getLoginHeaders()), String.class, null).getHeaders().get(TOKEN_NAME)
        validateResponse(result)
        X_SDS_AUTH_TOKEN = result.get(0)
        return this
    }

    private void validateResponse(List<String> result) {
        if (result == null || result.isEmpty()) {
            throw new ECSAuthenticationProblemException()
        }
    }

    def getLoginHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders()
        httpHeaders.put(ecsConfig.getEcsManagementUsername(), getHeaderValueList(ecsConfig.getEcsManagementPassword()))
        return httpHeaders
    }
}
