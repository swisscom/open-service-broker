package com.swisscom.cloud.sb.broker.services.openwhisk

import com.swisscom.cloud.sb.broker.binding.BindResponseDto
import groovy.json.JsonBuilder

class OpenWhiskBindResponseDto implements BindResponseDto {
    String openwhiskExecutionUrl
    String openwhiskAdminUrl
    String openwhiskUUID
    String openwhiskKey
    String openwhiskNamespace
    String openwhiskSubject

    @Override
    String toJson() {
        def jsonBuilder = createBuilder()
        return jsonBuilder.toPrettyString()
    }

    protected JsonBuilder createBuilder() {
        def jsonBuilder = new JsonBuilder()
        jsonBuilder.credentials(
                executionUrl: openwhiskExecutionUrl,
                adminUrl: openwhiskAdminUrl,
                uuid: openwhiskUUID,
                key: openwhiskKey,
                namespace: openwhiskNamespace,
                subject: openwhiskSubject
        )
        return jsonBuilder
    }
}
