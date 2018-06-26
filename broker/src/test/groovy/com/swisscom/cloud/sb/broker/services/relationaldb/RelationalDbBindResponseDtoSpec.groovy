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

import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Specification

class RelationalDbBindResponseDtoSpec extends Specification {

    def 'json serialization should work correctly'() {
        given:
        RelationalDbBindResponseDto credentials = new RelationalDbBindResponseDto(database: 'database',
                username: 'username',
                password: 'password',
                host: 'host',
                port: 1234,
                vendor: 'vendor')
        and:
        String expected = """{
                            "credentials": {
                                "host": "host",
                                "hostname": "host",
                                "port": 1234,
                                "name": "database",
                                "database": "database",
                                "username": "username",
                                "password": "password",
                                "database_uri": "vendor://username:password@host:1234/database?reconnect=true",
                                "uri": "vendor://username:password@host:1234/database?reconnect=true",
                                "jdbcUrl":"jdbc:vendor://host:1234/database?user=username&password=password"
                            }
                        }"""
        expect:
        JSONAssert.assertEquals(expected, credentials.toJson(), false)
    }
}
