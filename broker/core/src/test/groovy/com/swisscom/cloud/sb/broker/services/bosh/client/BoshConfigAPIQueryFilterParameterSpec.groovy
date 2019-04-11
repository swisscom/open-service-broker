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

package com.swisscom.cloud.sb.broker.services.bosh.client

import spock.lang.Specification

class BoshConfigAPIQueryFilterParameterSpec extends Specification {
    def "happy path: query correctly built with name and type set"() {
        given:
        String name = 'test'
        String type = 'cloud'
        when:
        String filter = (new GenericConfigAPIQueryFilter.Builder()).withName(name).withType(type).withLatest(true).build().asUriString()
        then:
        "?name=${name}&type=${type}&latest=true" == filter
    }

    def "happy path: query correctly built with name set"() {
        given:
        String name = 'test'
        when:
        String filter = (new GenericConfigAPIQueryFilter.Builder()).withName(name).withLatest(true).build().asUriString()
        then:
        "?name=${name}&latest=true" == filter
    }

    def "happy path: query correctly built with type set"() {
        given:
        String type = 'cloud'
        when:
        String filter = (new GenericConfigAPIQueryFilter.Builder()).withType(type).withLatest(true).build().asUriString()

        then:
        "?type=${type}&latest=true" == filter
    }

    def "happy path: query correctly built without any params"() {
        when:
        String filter = (new GenericConfigAPIQueryFilter.Builder()).withLatest(true).build().asUriString()
        then:
        "?latest=true" == filter
    }
}
