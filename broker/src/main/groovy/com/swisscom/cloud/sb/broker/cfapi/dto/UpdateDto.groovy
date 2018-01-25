package com.swisscom.cloud.sb.broker.cfapi.dto

import org.hibernate.validator.constraints.NotBlank

class UpdateDto implements Serializable {
    @NotBlank
    String service_id

    String plan_id
    Map<String, Object> parameters
    PreviousValuesDto previous_values
}
