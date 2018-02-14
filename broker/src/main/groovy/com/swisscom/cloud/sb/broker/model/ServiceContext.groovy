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

    static ServiceContext from(String key, String value) { return new ServiceContext(key: key, value: value) }

}
