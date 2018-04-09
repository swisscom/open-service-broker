package com.swisscom.cloud.sb.broker.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.validator.constraints.NotBlank

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

@Entity
class Plan extends BaseModel{

    @NotBlank
    @Column(unique = true)
    String guid
    String name
    String description
    String templateUniqueIdentifier
    String templateVersion
    Boolean free
    @Column(columnDefinition = 'int default 0')
    int displayIndex
    String internalName
    String serviceProviderClass
    @Column(columnDefinition='tinyint(1) default 0')
    Boolean asyncRequired
    @Column(columnDefinition = 'int default 0')
    Integer maxBackups
    @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name="plan_id")
    Set<Parameter> parameters = []
    @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name="plan_id")
    Set<PlanMetadata> metadata = []

    @ManyToOne
    @JoinColumn(name="service_id")
    @JsonIgnore
    CFService service

    String serviceInstanceCreateSchema
    String serviceInstanceUpdateSchema
    String serviceBindingCreateSchema
}
