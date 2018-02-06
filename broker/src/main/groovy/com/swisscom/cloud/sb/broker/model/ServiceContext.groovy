package com.swisscom.cloud.sb.broker.model

import com.fasterxml.jackson.annotation.JsonIgnore

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
class ServiceContext extends BaseModel {

    @Column(name = '_key')
    String key
    @Column(name = '_value')
    String value

    @ManyToOne
    @JoinColumn(name = 'service_instance_id')
    @JsonIgnore
    ServiceInstance serviceInstance

    static ServiceContext from(String key, String value) { return new ServiceContext(key: key, value: value) }

}
