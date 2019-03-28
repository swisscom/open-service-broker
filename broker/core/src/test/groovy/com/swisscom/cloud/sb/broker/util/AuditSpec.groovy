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

package com.swisscom.cloud.sb.broker.util

import spock.lang.Specification


class AuditSpec extends Specification {

    def "Log with message and parameters"() {
        when:
        String message = "This is a test"

        then:
        null == Audit.log(message)
        noExceptionThrown()
    }

    def "Log with simple message"() {
        when:
        String message = "This is a test"
        def parameters = [key: "value", obj: [well: [well: 'well']]]

        then:
        null == Audit.log(message, parameters)
        noExceptionThrown()
    }

    def 'Can serialize Parameters correctly'() {
        given:
        def parameters = [key: "value", obj: [well: [well: 'well']]]

        when:
        String json = Audit.stringifyParameters(parameters)

        then:
        json == 'key:"value",obj:{"well":{"well":"well"}}'
    }
}