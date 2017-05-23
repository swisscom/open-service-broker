package com.swisscom.cf.broker.cfapi.dto

import org.hibernate.validator.constraints.NotBlank


class ProvisioningDto implements Serializable {
    @NotBlank
    String service_id
    @NotBlank
    String plan_id
    String organization_guid
    String space_guid
    Map<String, Object> parameters

    @Override
    public String toString() {
        return "ProvisioningDto{" +
                "service_id='" + service_id + '\'' +
                ", plan_id='" + plan_id + '\'' +
                ", organization_guid='" + organization_guid + '\'' +
                ", space_guid='" + space_guid + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}
