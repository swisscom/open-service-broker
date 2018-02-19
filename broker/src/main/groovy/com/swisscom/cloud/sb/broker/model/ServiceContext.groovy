package com.swisscom.cloud.sb.broker.model

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.validation.constraints.NotNull

@Entity
class ServiceContext extends BaseModel {

    @NotNull
    String platform

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_context_id")
    Set<ServiceContextDetail> details = []

}
