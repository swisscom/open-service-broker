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

package com.swisscom.cloud.sb.broker.services.relationaldb

import com.swisscom.cloud.sb.broker.services.credential.BindResponseDto
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
