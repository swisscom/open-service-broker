package com.swisscom.cf.broker.services.ecs.facade.client.details

import com.google.common.annotations.VisibleForTesting
import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.facade.client.details.exceptions.ECSAuthenticationProblemException
import com.swisscom.cf.broker.util.RestTemplateFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

class TokenManager {

    @VisibleForTesting
    private static String X_SDS_AUTH_TOKEN = "STARTUP_DEFAULT_TOKEN"
    private static String TOKEN_NAME = "X-SDS-AUTH-TOKEN"
    @VisibleForTesting
    private final ECSConfig ecsConfig
    @VisibleForTesting
    private final RestTemplateFactory restTemplateFactory

    TokenManager(ECSConfig ecsConfig, RestTemplateFactory restTemplateFactory) {
        this.ecsConfig = ecsConfig
        this.restTemplateFactory = restTemplateFactory
    }

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

    def refreshAuthToken() {
        try {
            //TODO perhaps synchronize block on class or static X_SDS_AUTH_TOKEN
            HttpHeaders httpHeaders = restTemplateFactory.buildWithBasicAuthentication(ecsConfig.ecsManagementUsername, ecsConfig.ecsManagementPassword).exchange(
                    ecsConfig.getEcsManagementBaseUrl() + "/login",
                    HttpMethod.GET,
                    new HttpEntity<Object>(),
                    String.class).getHeaders()
            X_SDS_AUTH_TOKEN = httpHeaders.get(TOKEN_NAME).get(0)
            return this
        } catch (HttpClientErrorException clientErrorException) {
            if (clientErrorException.statusCode == HttpStatus.UNAUTHORIZED) {
                throw new ECSAuthenticationProblemException()
            }
            throw clientErrorException
        }
    }
}
