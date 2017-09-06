package com.swisscom.cloud.sb.broker.services.kubernetes.dto

import com.swisscom.cloud.sb.broker.binding.BindResponseDto
import groovy.json.JsonBuilder

class RedisBindResponseDto implements BindResponseDto {
    String host
    int masterPort
    List<Integer> slavePorts
    String password


    String getUri() {
        return "redis://:${password}@${host}:${masterPort}"
    }

    @Override
    String toJson() {
        def jsonBuilder = new JsonBuilder()
        jsonBuilder.credentials(
                host: host,
                port: masterPort,
                master_port: masterPort,
                slave_ports: slavePorts,
                password: password
        )
        return jsonBuilder.toPrettyString()
    }
}
