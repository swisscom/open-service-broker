package com.swisscom.cloud.sb.broker.model

import com.fasterxml.jackson.annotation.JsonIgnore

import javax.persistence.*

@Entity
@Table(name = 'service_metadata')
class CFServiceMetadata extends BaseModel {

    @Column(name = '_key')
    String key
    @Column(name = '_value')
    String value
    @Column(name = '_type', columnDefinition = "varchar(255) default 'String'")
    String type
    @ManyToOne
    @JoinColumn(name = "service_id")
    @JsonIgnore
    CFService service
}