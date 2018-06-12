package com.swisscom.cloud.sb.broker.model

import com.fasterxml.jackson.annotation.JsonIgnore

import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = 'service_permission')

class CFServicePermission extends BaseModel {

    public static final SYSLOG_DRAIN = "syslog_drain"

    String permission

    @ManyToOne
    @JoinColumn(name = "cf_service_id")
    @JsonIgnore
    CFService service
}
