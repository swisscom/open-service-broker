package com.swisscom.cloud.sb.broker.model

import com.fasterxml.jackson.annotation.JsonIgnore

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = 'application_user')
class ApplicationUser extends BaseModel {

    @Column(unique = true)
    String username
    @JsonIgnore
    String password
    Boolean enabled
    String role
    String platformGuid
}
