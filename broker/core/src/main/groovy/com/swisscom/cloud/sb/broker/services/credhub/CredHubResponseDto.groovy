package com.swisscom.cloud.sb.broker.services.credhub

import com.swisscom.cloud.sb.broker.services.credential.BindResponseDto
import groovy.json.JsonBuilder

class CredHubResponseDto implements BindResponseDto{

    String credhubName

    @Override
    String toJson() {
        def jsonBuilder = createBuilder()
        return jsonBuilder.toPrettyString()
    }

    protected JsonBuilder createBuilder() {
        def jsonBuilder = new JsonBuilder()
        jsonBuilder.credentials(
                "credhub-ref": credhubName
        )
        return jsonBuilder
    }
}
