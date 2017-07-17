package com.swisscom.cloud.sb.broker.services.kubernetes.endpoint.parameters

import com.swisscom.cloud.sb.broker.services.kubernetes.endpoint.EndpointMapper
import groovy.transform.CompileStatic
import org.springframework.data.util.Pair
import org.springframework.stereotype.Component

@CompileStatic
@Component
class EndpointMapperParamsDecorated {

    Pair<String, ?> getEndpointUrlByTypeWithParams(String templateType, Map<String, String> paramMap) {
        String endpointUrl = EndpointMapper.INSTANCE.getEndpointUrlByType(templateType).getFirst()
        for (String key : paramMap.keySet()) {
            endpointUrl = endpointUrl.replaceAll(key, paramMap.get(key))
        }
        return Pair.of(endpointUrl, EndpointMapper.INSTANCE.getEndpointUrlByType(templateType).getSecond())
    }

}
