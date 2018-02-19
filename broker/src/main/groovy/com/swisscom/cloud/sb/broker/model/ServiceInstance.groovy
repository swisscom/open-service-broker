package com.swisscom.cloud.sb.broker.model

import org.hibernate.validator.constraints.NotBlank

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

@Entity
class ServiceInstance extends BaseModel{
    @NotBlank
    @Column(unique = true)
    String guid
    String org
    String space

    Date dateCreated = new Date()
    @Column(columnDefinition='tinyint(1) default 1')
    boolean completed
    @Column(columnDefinition='tinyint(1) default 0')
    boolean deleted
    @OneToMany
    @JoinColumn(name="service_instance_id")
    List<ServiceBinding> bindings = []
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "service_instance_service_detail",
            joinColumns = @JoinColumn(name = "service_instance_details_id"),
            inverseJoinColumns = @JoinColumn(name = "service_detail_id"))
    List<ServiceDetail> details = []
    @ManyToOne
    @JoinColumn(name="plan_id")
    Plan plan
    @ManyToOne
    @JoinColumn(name = "parent_service_instance_id")
    ServiceInstance parentServiceInstance
    @OneToMany(mappedBy = "id", fetch = FetchType.EAGER)
    Set<ServiceInstance> childs = []

    @Override
    public String toString() {
        return "ServiceInstance{" +
                "id=" + id +
                ", guid='" + guid + '\'' +
                ", org='" + org + '\'' +
                ", space='" + space + '\'' +
                ", dateCreated=" + dateCreated +
                ", completed=" + completed +
                ", deleted=" + deleted +
                '}'
    }
}
