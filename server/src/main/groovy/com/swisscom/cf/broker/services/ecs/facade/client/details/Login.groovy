package com.swisscom.cf.broker.services.ecs.facade.client.details

import org.springframework.http.HttpHeaders


class Login {

    private static String X_SDS_AUTH_TOKEN

    HttpHeaders getHeaders() {
        //return "return headers with old token - X-SDS-AUTH-TOKEN"
        return null
    }

    def refreshHeaders() {
        //"replace X-SDS-AUTH-TOKEN for new token! Use RestTemplateFactory"
        return this
    }
}
