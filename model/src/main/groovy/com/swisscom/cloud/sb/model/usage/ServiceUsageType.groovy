package com.swisscom.cloud.sb.model.usage

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum ServiceUsageType {
    TRANSACTIONS("transactions"),
    WATERMARK("watermark"),

    final String type

    ServiceUsageType(final String type) {
        this.type = type
    }

    String toString() {
        type
    }

    @JsonValue
    public String getValue() {
        return type
    }

    @JsonCreator
    public static ServiceUsageType fromString(String key) {
        return key ? ServiceUsageType.values().find { it.value == key } : null
    }
}
