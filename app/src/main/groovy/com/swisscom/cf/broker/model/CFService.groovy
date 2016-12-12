package com.swisscom.cf.broker.model

import javax.persistence.*

@Entity
@Table(name = 'service')
class CFService extends BaseModel{

    @Column(unique = true)
    String guid
    @Column(unique = true)
    String name
    String description
    Boolean bindable
    String internalName
    @Column(columnDefinition = 'int default 0')
    int displayIndex
    @Column(columnDefinition='tinyint(1) default 0')
    Boolean asyncRequired

    String dashboardClientId
    String dashboardClientSecret
    String dashboardClientRedirectUri

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name="cf_service_id")
    Set<Tag> tags = []
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name="service_id")
    Set<Plan> plans = []
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name="service_id")
    Set<CFServiceMetadata> metadata = []
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name="cf_service_id")
    Set<CFServicePermission> permissions = []
}
