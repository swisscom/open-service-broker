/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.broker.model

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
    String serviceProviderClass
    @Column(columnDefinition = 'int default 0')
    int displayIndex
    @Column(columnDefinition='tinyint(1) default 0')
    Boolean plan_updateable
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

    @Column(columnDefinition = 'tinyint(1) default 0')
    Boolean instancesRetrievable
    @Column(columnDefinition = 'tinyint(1) default 0')
    Boolean bindingsRetrievable

}
