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

package com.swisscom.cloud.sb.broker.services.mongodb

import com.swisscom.cloud.sb.broker.binding.BindResponseDto
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
