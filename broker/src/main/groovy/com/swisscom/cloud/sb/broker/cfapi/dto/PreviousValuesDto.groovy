package com.swisscom.cloud.sb.broker.cfapi.dto

class PreviousValuesDto implements Serializable {
    @Deprecated
    String service_id
    String plan_id
    @Deprecated
    String organization_id
    @Deprecated
    String space_id

    @Override
    String toString() {
        return "PreviousValuesDto{" +
                "service_id='" + service_id + '\'' +
                ", plan_id='" + plan_id + '\'' +
                ", organization_guid='" + organization_id + '\'' +
                ", space_guid='" + space_id + '\'' +
                '}'
    }
}
