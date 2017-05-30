package com.swisscom.cloud.sb.broker.services.mongodb.enterprise

import com.swisscom.cloud.sb.broker.services.mongodb.MongoDbBindResponseDto
import groovy.json.JsonBuilder

class MongoDbEnterpriseBindResponseDto extends MongoDbBindResponseDto {

    String opsManagerUrl
    String opsManagerUser
    String opsManagerPassword
    String replicaSet

    @Override
    String toJson() {
        def options = ["replicaSet": replicaSet]

        def jsonBuilder = new JsonBuilder()
        jsonBuilder.credentials(
                host: hosts?.join(","),
                port: port,
                database: database,
                username: username,
                password: password,
                database_uri: getUri(options),
                uri: getUri(options),
                ops_manager_url: opsManagerUrl,
                ops_manager_user: opsManagerUser,
                ops_manager_password: opsManagerPassword,
                replica_set: replicaSet
        )
        return jsonBuilder.toPrettyString()
    }
}
