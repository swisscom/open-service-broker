package com.swisscom.cf.broker.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
class DeprovisionRequest extends BaseModel{

    boolean acceptsIncomplete
    @Column(unique = true)
    String serviceInstanceGuid

    @ManyToOne
    @JoinColumn(name="service_instance_id")
    ServiceInstance serviceInstance

    @Override
    public String toString() {
        return "DeprovisionRequest{" +
                "id=" + id +
                ", acceptsIncomplete=" + acceptsIncomplete +
                ", serviceInstanceGuid='" + serviceInstanceGuid +
                '}';
    }
}
