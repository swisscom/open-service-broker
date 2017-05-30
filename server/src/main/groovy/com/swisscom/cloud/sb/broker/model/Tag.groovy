package com.swisscom.cloud.sb.broker.model

import com.fasterxml.jackson.annotation.JsonIgnore

import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
class Tag extends BaseModel{

    String tag

    @ManyToOne
    @JoinColumn(name="cf_service_id")
    @JsonIgnore
    CFService cfService
}
