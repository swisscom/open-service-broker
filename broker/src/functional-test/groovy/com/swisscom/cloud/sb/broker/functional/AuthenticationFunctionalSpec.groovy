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

package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.client.ServiceBrokerClient
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

class AuthenticationFunctionalSpec extends BaseFunctionalSpec {

    def "catalog controller returns 401 when no credentials provided"() {
        when:
        new ServiceBrokerClient(appBaseUrl,null,null).getCatalog()

        then:
        def ex = thrown(HttpClientErrorException)
        ex.statusCode == HttpStatus.UNAUTHORIZED
    }

    def "catalog controller returns 401 when wrong credentials provided"() {
        when:
        new ServiceBrokerClient(appBaseUrl,'SomeUsername','WrongPassword').getCatalog()

        then:
        def ex = thrown(HttpClientErrorException)
        ex.statusCode == HttpStatus.UNAUTHORIZED
    }

    def "catalog controller returns 200 when correct credentials provided"() {
        when:
        def response = serviceBrokerClient.getCatalog()

        then:
        response.statusCode == HttpStatus.OK
    }

    def "catalog controller returns Forbidden 403 when wrong role provided"() {
        when:
        new ServiceBrokerClient(appBaseUrl, cfExtUser.username, cfExtUser.password).getCatalog()

        then:
        def ex = thrown(HttpClientErrorException)
        ex.statusCode == HttpStatus.FORBIDDEN
    }
}
