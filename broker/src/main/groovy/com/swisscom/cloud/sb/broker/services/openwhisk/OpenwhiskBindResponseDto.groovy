package com.swisscom.cloud.sb.broker.services.openwhisk

import com.swisscom.cloud.sb.broker.binding.BindResponseDto
import groovy.json.JsonBuilder

class OpenwhiskBindResponseDto implements BindResponseDto {
    String user
    String pass

    @Override
    String toJson() {
        def jsonBuilder = createBuilder()
        return jsonBuilder.toPrettyString()
    }

    protected JsonBuilder createBuilder() {
        def jsonBuilder = new JsonBuilder()
        jsonBuilder.credentials(
                username: user,
                password: pass
        )
        return jsonBuilder
    }
}
