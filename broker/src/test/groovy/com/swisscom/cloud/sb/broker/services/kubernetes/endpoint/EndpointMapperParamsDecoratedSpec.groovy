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
