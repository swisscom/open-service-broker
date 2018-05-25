package com.swisscom.cloud.sb.model.usage.extended

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.swisscom.cloud.sb.model.usage.ServiceUsageType

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
class ServiceUsageItem {
    String property
    String value
    String unit

    @JsonSerialize
    @JsonProperty("type")
    ServiceUsageType type

    @Override
    public String toString() {
        return "ServiceUsageItem{" +
                "property=" + property +
                ", value='" + value + '\'' +
                ", unit=" + unit +
                ", type=" + type +
                '}';
    }
}
