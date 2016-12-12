package com.swisscom.cf.broker.cfapi.dto

import org.hibernate.validator.constraints.NotBlank

class BindRequestDto implements Serializable {
    @NotBlank
    String service_id
    @NotBlank
    String plan_id

    String app_guid
    Map parameters
}
