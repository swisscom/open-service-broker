/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

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
