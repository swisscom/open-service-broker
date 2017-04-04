package com.swisscom.cf.broker.services.ecs.dto

import com.swisscom.cf.broker.binding.BindResponseDto
import groovy.json.JsonBuilder


class ECSBindResponse implements BindResponseDto {
    String accessHost
    String accessKey
    String sharedSecret
    String namespace

    @Override
    String toJson() {
        def jsonBuilder = new JsonBuilder()
        jsonBuilder.credentials(
                accessHost: "${accessHost}",
                accessKey: "${accessKey}",
                sharedSecret: "${sharedSecret}",
                namespace: "${namespace}",
        )
        return jsonBuilder.toPrettyString()
    }
}
