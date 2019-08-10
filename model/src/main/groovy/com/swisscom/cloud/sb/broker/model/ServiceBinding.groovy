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
import javax.validation.constraints.NotBlank

@Entity
class ServiceBinding extends BaseModel {

    @NotBlank
    @Column(unique = true)
    String guid
    @Column(columnDefinition = 'text')
    String credentials //Credential in JSON format
    String parameters
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "service_binding_service_detail",
            joinColumns = @JoinColumn(name = "service_binding_details_id"),
            inverseJoinColumns = @JoinColumn(name = "service_detail_id"))
    Set<ServiceDetail> details = []

    @OneToOne(fetch = FetchType.LAZY)
    ServiceContext serviceContext

    @Column(name = 'service_instance_id', updatable = false, insertable = false)
    Integer serviceInstanceId

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = 'service_instance_id')
    @JsonIgnore
    ServiceInstance serviceInstance

    @ManyToOne(fetch = FetchType.LAZY)
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
