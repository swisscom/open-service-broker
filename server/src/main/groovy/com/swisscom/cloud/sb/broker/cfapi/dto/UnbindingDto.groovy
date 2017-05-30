package com.swisscom.cloud.sb.broker.cfapi.dto

class UnbindingDto implements Serializable {
    String service_id
    String plan_id
    static constraints = {
        service_id nullable: true
        plan_id nullable: true
    }
}
