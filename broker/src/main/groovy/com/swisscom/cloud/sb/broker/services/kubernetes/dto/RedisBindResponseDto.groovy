package com.swisscom.cloud.sb.broker.services.kubernetes.dto

import com.swisscom.cloud.sb.broker.binding.BindResponseDto
import groovy.json.JsonBuilder

class RedisBindResponseDto implements BindResponseDto {
    String host
    int port
    String password


    String getUri() {
        return "redis://:${password}@${host}:${port}"
    }

    @Override
    String toJson() {
        def jsonBuilder = new JsonBuilder()
        jsonBuilder.credentials(
                host: host,
                port: port,
                password: password
        )
        return jsonBuilder.toPrettyString()
    }
}
