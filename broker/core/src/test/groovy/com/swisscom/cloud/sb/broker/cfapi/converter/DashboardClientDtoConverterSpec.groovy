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

package com.swisscom.cloud.sb.broker.cfapi.converter

import com.swisscom.cloud.sb.broker.model.CFService
import spock.lang.Specification


class DashboardClientDtoConverterSpec extends Specification {
    DashboardClientDtoConverter dashboardClientDtoConverter

    def setup() {
        dashboardClientDtoConverter = new DashboardClientDtoConverter()
    }

    def "dashboard clientId,clientSecret fields should be non-null for conversion"() {
        expect:
        null == dashboardClientDtoConverter.convert(source)
        where:
        source << [new CFService(dashboardClientSecret: null, dashboardClientId: 'id', dashboardClientRedirectUri: 'uri'),
                   new CFService(dashboardClientSecret: 'secret', dashboardClientId: null, dashboardClientRedirectUri: 'uri')]
    }

    def "conversion is done correctly"() {
        given:
        def secret = 'secret'
        def id = 'id'
        def uri = 'uri'
        when:
        def result = dashboardClientDtoConverter.convert(new CFService(dashboardClientSecret: secret, dashboardClientId: id, dashboardClientRedirectUri: uri))
        then:
        result.secret == secret
        result.id == id
        result.redirect_uri == uri
    }
}
