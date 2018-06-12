package com.swisscom.cloud.sb.broker.model

import org.hibernate.validator.constraints.NotBlank

import javax.persistence.*

@Entity
class ServiceInstance extends BaseModel {
    @NotBlank
    @Column(unique = true)
    String guid
    Date dateCreated = new Date()
    @Column(columnDefinition = 'tinyint(1) default 1')
    boolean completed
    @Column(columnDefinition = 'tinyint(1) default 0')
    boolean deleted
    @Column
    Date dateDeleted
    @Column
    String parameters
    @OneToMany
    @JoinColumn(name = "service_instance_id")
    List<ServiceBinding> bindings = []
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "service_instance_service_detail",
            joinColumns = @JoinColumn(name = "service_instance_details_id"),
            inverseJoinColumns = @JoinColumn(name = "service_detail_id"))
    List<ServiceDetail> details = []
    @ManyToOne
    @JoinColumn(name = "plan_id")
    Plan plan
    @ManyToOne
    @JoinColumn(name = "parent_service_instance_id")
    ServiceInstance parentServiceInstance
    @OneToMany(mappedBy = "parentServiceInstance", fetch = FetchType.EAGER)
    Set<ServiceInstance> childs = []
    @OneToOne
    ServiceContext serviceContext
    @ManyToOne
    @JoinColumn(name = "application_user_id")
    ApplicationUser applicationUser

    @Override
    String toString() {
        return "ServiceInstance{" +
                "id=" + id +
                ", guid='" + guid + '\'' +
                ", dateCreated=" + dateCreated +
                ", completed=" + completed +
                ", deleted=" + deleted +
                ", dateDeleted=" + dateDeleted +
                ", parameters=" + parameters +
                "}"
    }
}
