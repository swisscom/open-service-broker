package com.swisscom.cloud.sb.broker.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.validator.constraints.NotBlank

import javax.persistence.*

@Entity
class ServiceBinding extends BaseModel{

    @NotBlank
    @Column(unique = true)
    String guid
    @Column(columnDefinition='text')
    String credentials //Credential in JSON format

    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "service_binding_service_detail",
            joinColumns = @JoinColumn(name = "service_binding_details_id"),
            inverseJoinColumns = @JoinColumn(name = "service_detail_id"))
    Set<ServiceDetail> details = []

    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "service_binding_service_context",
            joinColumns = @JoinColumn(name = "service_binding_id"),
            inverseJoinColumns = @JoinColumn(name = "service_context_id"))
    Set<ServiceContext> contexts = []

    @ManyToOne
    @JoinColumn(name = 'service_instance_id')
    @JsonIgnore
    ServiceInstance serviceInstance
}
