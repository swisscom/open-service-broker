package com.swisscom.cloud.sb.broker.model

import com.fasterxml.jackson.annotation.JsonIgnore

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
class ServiceContextDetail extends BaseModel {

    @Column(name = '_key')
    String key
    @Column(name = '_value')
    String value

    @ManyToOne
    @JoinColumn(name = "service_context_id")
    @JsonIgnore
    ServiceContext serviceContext

    static ServiceContextDetail from(String key, String value) {
        return new ServiceContextDetail(key: key, value: value)
    }

}
