package com.swisscom.cf.broker.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.swisscom.cf.broker.model.Parameter
import org.hibernate.validator.constraints.NotBlank

import javax.persistence.*

@Entity
class Plan extends BaseModel{

    @NotBlank
    @Column(unique = true)
    String guid
    String name
    String description
    String templateUniqueIdentifier
    Boolean free
    @Column(columnDefinition = 'int default 0')
    int displayIndex
    String internalName
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
}
