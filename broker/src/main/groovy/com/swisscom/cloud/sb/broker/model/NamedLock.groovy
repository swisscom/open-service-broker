package com.swisscom.cloud.sb.broker.model

import javax.persistence.Column
import javax.persistence.Entity

@Entity
class NamedLock extends BaseModel {
    String name
    @Column(insertable=false)
    Date dateCreated
    Integer ttlInSeconds = 1
}
