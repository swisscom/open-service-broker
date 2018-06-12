package com.swisscom.cloud.sb.broker.model

import com.fasterxml.jackson.annotation.JsonIgnore

import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
class Parameter extends BaseModel {

    String name
    String description
    String value
    String template

    @ManyToOne
    @JoinColumn(name = "plan_id")
    @JsonIgnore
    Plan plan
}