package com.swisscom.cloud.sb.model.usage

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum ServiceUsageUnit {
    GIGABYTE_SECOND("GB-s"),
    MEGABYTE_SECOND("MB-s")

    final String unit

    ServiceUsageUnit(String unit) {
        this.unit = unit
    }

    String toString() {
        unit
    }

    @JsonValue
    String getValue() {
        return unit
    }

    @JsonCreator
    static ServiceUsageUnit fromString(String key) {
        return key ? ServiceUsageUnit.values().find { it.value == key } : null
    }
}