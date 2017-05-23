package com.swisscom.cf.broker.model

import com.fasterxml.jackson.annotation.JsonIgnore

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
class PlanMetadata extends BaseModel{

    @Column(name = '_key')
    String key
    @Column(name = '_value')
    String value
    @Column(name = '_type',columnDefinition="varchar(255) default 'String'")
    String type

    @ManyToOne
    @JoinColumn(name="plan_id")
    @JsonIgnore
    Plan plan

    static mapping = {
        key column: '_key'
        value column: '_value', type: "text"
        type column: '_type', defaultValue: "'String'"
    }
}
