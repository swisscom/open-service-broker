package com.swisscom.cf.broker.filterextensions.serviceusage

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
class ServiceUsage {
    @JsonSerialize
    @JsonProperty("id")
    String value
    @JsonSerialize
    @JsonProperty("type")
    Type type
    @JsonSerialize
    @JsonProperty("end_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = 'yyyy-MM-ddThh:mm:ssZ')
    Date enddate

    @Override
    public String toString() {
        return "ServiceUsage{" +
                "value='" + value + '\'' +
                ", type=" + type +
                ", enddate=" + enddate +
                '}';
    }

    enum Type {
        TRANSACTIONS("transactions"),
        WATERMARK("watermark"),

        final String type

        Type(final String type) {
            this.type = type
        }

        String toString() {
            type
        }
    }
}
