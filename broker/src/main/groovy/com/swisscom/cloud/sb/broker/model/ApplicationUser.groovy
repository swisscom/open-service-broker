package com.swisscom.cloud.sb.broker.model

import javax.persistence.*

@Entity
@Table(name = 'application_user')
class ApplicationUser extends BaseModel{

    @Column(unique = true)
    String username
    String password
    Boolean enabled
    String role
    String platformGuid
}
