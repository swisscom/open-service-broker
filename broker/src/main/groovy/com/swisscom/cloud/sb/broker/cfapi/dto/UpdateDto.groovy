package com.swisscom.cloud.sb.broker.cfapi.dto

import org.hibernate.validator.constraints.NotBlank
import org.springframework.cloud.servicebroker.model.Context

class UpdateDto implements Serializable {
    @NotBlank
    String service_id
    String plan_id
    Context context
    Map<String, Object> parameters
    PreviousValuesDto previous_values

    @Override
    String toString() {
        return "UpdateDto{" +
                "service_id='" + service_id + '\'' +
                ", plan_id='" + plan_id + '\'' +
                ", context='" + context + '\'' +
                ", parameters=" + parameters +
                ", previous_values=" + previous_values +
                '}'
    }
}
