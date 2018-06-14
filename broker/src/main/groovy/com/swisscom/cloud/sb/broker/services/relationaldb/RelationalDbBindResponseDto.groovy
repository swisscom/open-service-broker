package com.swisscom.cloud.sb.broker.services.relationaldb

import com.swisscom.cloud.sb.broker.binding.BindResponseDto
import groovy.json.JsonBuilder


class RelationalDbBindResponseDto implements BindResponseDto {
    String host
    int port
    String database
    String username
    String password
    String vendor


    String getUri() {
        return "${vendor}://${username}:${password}@${this.getHost()}:${this.getPort()}/${database}?reconnect=true"
    }

    String getJdbcUrl() {
        return "jdbc:${vendor}://${this.getHost()}:${this.getPort()}/${database}?user=${username}&password=${password}"
    }

    @Override
    String toJson() {
        def jsonBuilder = new JsonBuilder()
        jsonBuilder.credentials(
                host: host,
                hostname: host,
                port: port,
                name: database,
                database: database,
                username: username,
                password: password,
                database_uri: getUri(),
                uri: getUri(),
                jdbcUrl: getJdbcUrl()
        )
        return jsonBuilder.toPrettyString()
    }
}
