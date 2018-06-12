package com.swisscom.cloud.sb.broker.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.validator.constraints.NotBlank

import javax.persistence.*

@Entity
class ServiceBinding extends BaseModel {

    @NotBlank
    @Column(unique = true)
    String guid
    @Column(columnDefinition = 'text')
    String credentials //Credential in JSON format
    String parameters
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "service_binding_service_detail",
            joinColumns = @JoinColumn(name = "service_binding_details_id"),
            inverseJoinColumns = @JoinColumn(name = "service_detail_id"))
    Set<ServiceDetail> details = []

    @OneToOne
    ServiceContext serviceContext

    @ManyToOne
    @JoinColumn(name = 'service_instance_id')
    @JsonIgnore
    ServiceInstance serviceInstance

    @ManyToOne
    @JoinColumn(name = "application_user_id")
    ApplicationUser applicationUser

    String credhubCredentialId

    @Override
    String toString() {
        return "ServiceBinding{" +
                "id=" + id +
                ", guid='" + guid + '\'' +
                ", credentials=" + credentials +
                ", parameters=" + parameters +
                ", details=" + details +
                ", serviceContext=" + serviceContext +
                "}"
    }
}
