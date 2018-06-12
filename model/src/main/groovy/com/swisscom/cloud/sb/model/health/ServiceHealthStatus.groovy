package com.swisscom.cloud.sb.model.health

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum ServiceHealthStatus {
    UNDEFINED("undefined"),
    OK("ok"),
    NOK("nok"),

    final String value

    ServiceHealthStatus(final String value) {
        this.value = value
    }

    String toString() {
        value
    }

    @JsonValue
    public String getValue() {
        return value
    }

    @JsonCreator
    public static ServiceHealthStatus fromString(String key) {
        return key ? ServiceHealthStatus.values().find { it.value == key } : null
    }
}
