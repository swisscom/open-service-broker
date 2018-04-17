package com.swisscom.cloud.sb.broker.cfapi.dto

import org.hibernate.validator.constraints.NotBlank
import org.springframework.cloud.servicebroker.model.Context

class BindRequestDto implements Serializable {
    @NotBlank(message = "service_id must not be blank")
    String service_id
    @NotBlank(message = "plan_id must not be blank")
    String plan_id

    String app_guid
    Map parameters
    Context context
}
