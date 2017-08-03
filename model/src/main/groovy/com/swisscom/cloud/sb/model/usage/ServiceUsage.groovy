package com.swisscom.cloud.sb.model.usage

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
class ServiceUsage implements Serializable{
    @JsonSerialize
    @JsonProperty("value")
    String value
    @JsonSerialize
    @JsonProperty("unit")
    ServiceUsageUnit unit
    @JsonSerialize
    @JsonProperty("type")
    ServiceUsageType type
    @JsonSerialize
    @JsonProperty("end_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ssZ")
    Date enddate

    @Override
    public String toString() {
        return "ServiceUsage{" +
                "value='" + value + '\'' +
                ", unit=" + unit +
                ", type=" + type +
                ", enddate=" + enddate +
                '}';
    }
}
