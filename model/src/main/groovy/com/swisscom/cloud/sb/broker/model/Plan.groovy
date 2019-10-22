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

import com.fasterxml.jackson.annotation.JsonIgnore

import javax.persistence.*

@Entity
class Plan extends BaseModel{

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
    @Column(columnDefinition='tinyint(1) default 1')
    Boolean active = true
    @Column(columnDefinition = 'int default 0')
    Integer maxBackups
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name="plan_id")
    Set<Parameter> parameters = []
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name="plan_id")
    Set<PlanMetadata> metadata = []

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="service_id")
    @JsonIgnore
    CFService service

    String serviceInstanceCreateSchema
    String serviceInstanceUpdateSchema
    String serviceBindingCreateSchema
}
