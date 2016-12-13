package com.swisscom.cf.broker.model

import com.fasterxml.jackson.annotation.JsonIgnore

import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.MappedSuperclass

@MappedSuperclass
class BaseModel {
    @Id
    @GeneratedValue
    @JsonIgnore
    Integer id
}
