package com.swisscom.cf.broker.services.ecs.facade.client.commands


class Login {

    String getHeaders() {
        return "return headers with old token - X-SDS-AUTH-TOKEN"
    }

    def refreshHeaders() {
        //"replace X-SDS-AUTH-TOKEN for new token!"
        return this
    }
}
