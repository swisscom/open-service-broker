package com.swisscom.cloud.sb.broker.cfapi.dto

import org.hibernate.validator.constraints.NotBlank
import org.springframework.cloud.servicebroker.model.Context


class ProvisioningDto implements Serializable {
    @NotBlank
    String service_id
    @NotBlank
    String plan_id
    @Deprecated
    String organization_guid
    @Deprecated
    String space_guid
    Context context
    Map<String, Object> parameters

    @Override
    String toString() {
        return "ProvisioningDto{" +
                "service_id='" + service_id + '\'' +
                ", plan_id='" + plan_id + '\'' +
                ", organization_guid='" + organization_guid + '\'' +
                ", space_guid='" + space_guid + '\'' +
                ", context='" + context + '\'' +
                ", parameters=" + parameters +
                '}'
    }
}
