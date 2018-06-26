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

package com.swisscom.cloud.sb.broker.services.kubernetes.endpoint

import com.swisscom.cloud.sb.broker.services.kubernetes.dto.NamespaceResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.ServiceAccountsResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.endpoint.parameters.EndpointMapperParamsDecorated
import org.springframework.data.util.Pair
import spock.lang.Specification

class EndpointMapperParamsDecoratedSpec extends Specification {

    EndpointMapperParamsDecorated endpointMapperParamsDecorated


    def setup() {
        endpointMapperParamsDecorated = new EndpointMapperParamsDecorated()
    }

    def "exchange returns the right endpoint url for Namespace type"() {
        given:
        Pair<String, NamespaceResponse> result = endpointMapperParamsDecorated.getEndpointUrlByTypeWithParams("Namespace", Collections.emptyMap())
        expect:
        result.getFirst() == "/api/v1/namespaces"
    }

    def "exchange returns the right endpoint url for ServiceAccount type"() {
        given:
        Pair<String, ServiceAccountsResponse> result = endpointMapperParamsDecorated.getEndpointUrlByTypeWithParams("ServiceAccount", Collections.emptyMap())
        expect:
        result.getFirst() == "/api/v1/namespaces/serviceInstanceGuid/serviceaccounts"
    }

    def "exchange returns the right endpoint url for ServiceAccount type with parameter"() {
        given:

        Pair<String, ServiceAccountsResponse> result = endpointMapperParamsDecorated.getEndpointUrlByTypeWithParams("ServiceAccount", getParams())
        expect:
        result.getFirst() == "/api/v1/namespaces/ID/serviceaccounts"
    }

    private Map getParams() {
        Map<String, String> params = new HashMap<>()
        params.put("serviceInstanceGuid", "ID")
        return params
    }
}
