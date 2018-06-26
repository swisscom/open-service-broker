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

import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Specification

class MongoDbEnterpriseBindResponseDtoSpec extends Specification {

    def "json serialization works correctly"() {
        given:
        MongoDbEnterpriseBindResponseDto credentials = new MongoDbEnterpriseBindResponseDto(database: 'database',
                username: 'username',
                password: 'password',
                hosts: ['host'],
                port: 1234,
                replicaSet: 'replicaSet1',
                opsManagerUrl: 'url',
                opsManagerUser: 'opsUser',
                opsManagerPassword: 'opsPw')
        and:
        String expected = """{
                            "credentials": {
                                "host": "host",
                                "port": "1234",
                                "database": "database",
                                "username": "username",
                                "password": "password",
                                "database_uri": "mongodb://username:password@host:1234/database?replicaSet=replicaSet1",
                                "uri": "mongodb://username:password@host:1234/database?replicaSet=replicaSet1",
                                "ops_manager_url": "url",
                                "ops_manager_user": "opsUser",
                                "ops_manager_password": "opsPw",
                                "replica_set": "replicaSet1"
                            }
                        }"""
        expect:
        JSONAssert.assertEquals(expected, credentials.toJson(), true)
    }
}
