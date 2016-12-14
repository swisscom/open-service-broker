package com.swisscom.cf.broker.services.mongodb

import com.swisscom.cf.broker.binding.BindResponseDto
import groovy.json.JsonBuilder

class MongoDbBindResponseDto implements BindResponseDto {
    String database
    String username
    String password
    List<String> hosts
    String port

    // mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database][?options]]
    String getUri(Map<String, String> options = null) {
        String hostPort = hosts.collect({ it + ":" + port }).join(",")
        def uri = "mongodb://${username}:${password}@${hostPort}/${database}"
        if (options) {
            return uri + "?" + options.collect({ k, v -> "${k}=${v}" }).join(",")
        } else {
            return uri
        }
    }

    @Override
    String toJson() {
        def jsonBuilder = createBuilder()
        return jsonBuilder.toPrettyString()
    }

    protected JsonBuilder createBuilder() {
        def jsonBuilder = new JsonBuilder()
        jsonBuilder.credentials(
                host: hosts?.join(","),
                port: port,
                database: database,
                username: username,
                password: password,
                database_uri: getUri(),
                uri: getUri()
        )
        return jsonBuilder
    }
}
