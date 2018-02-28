package com.swisscom.cloud.sb.broker.model

import com.fasterxml.jackson.annotation.JsonIgnore

import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.OneToOne

@Entity
class UpdateRequest extends BaseModel {
    String serviceInstanceGuid
    @OneToOne
    @JoinColumn(name = "plan_id")
    @JsonIgnore
    Plan plan
    @OneToOne
    @JoinColumn(name = "previous_plan_id")
    @JsonIgnore
    Plan previousPlan
    String parameters
    boolean acceptsIncomplete
    Date dateCreated = new Date()

    @Override
    String toString() {
        return "ProvisionRequest{" +
                "id=" + id +
                ", serviceInstanceGuid='" + serviceInstanceGuid + '\'' +
                ", plan=" + plan +
                ", previousPlan=" + previousPlan +
                ", parameters='" + parameters + '\'' +
                ", acceptsIncomplete=" + acceptsIncomplete +
                ", dateCreated='" + dateCreated +
                '}'
    }
}
