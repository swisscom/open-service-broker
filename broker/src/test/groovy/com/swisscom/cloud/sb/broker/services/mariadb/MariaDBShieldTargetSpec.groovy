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

package com.swisscom.cloud.sb.broker.services.mariadb

import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Specification


class MariaDBShieldTargetSpec extends Specification {
    def "generates an endpoint json for mysql"() {
        given:
        def target = new MariaDBShieldTarget(
                user: 'user1',
                password: 'pw1',
                host: 'host1',
                database: 'db1')
        and:
        String expected = """{
                            "mysql_user": "user1",
                            "mysql_password": "pw1",
                            "mysql_host": "host1",
                            "mysql_database": "db1",
                            "mysql_options": "${MariaDBShieldTarget.MYSQL_OPTIONS}"
                        }"""
        expect:
        JSONAssert.assertEquals(expected, target.endpointJson(), false)
    }

}