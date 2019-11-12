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

    @Column(unique = true)
    String guid
    @Column(columnDefinition = 'text')
    String credentials //Credential in JSON format
    String parameters
    @OneToMany(fetch =  FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "service_binding_service_detail",
            joinColumns = @JoinColumn(name = "service_binding_details_id"),
            inverseJoinColumns = @JoinColumn(name = "service_detail_id"))
    Set<ServiceDetail> details = []

    @OneToOne(fetch = FetchType.EAGER)
    ServiceContext serviceContext

    @Column(name = 'service_instance_id', updatable = false, insertable = false)
    Integer serviceInstanceId

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = 'service_instance_id')
    @JsonIgnore
    ServiceInstance serviceInstance

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "application_user_id")
    ApplicationUser applicationUser

    String credhubCredentialId

    /**
     * Get the value of the desired ServiceDetail identified by its key
     * FIXME WE should implement a equals in ServiceDetail using its key so we could use collections get
     *
     * @param key the identifier of the ServiceDetail
     * @return the value associated to the key
     */
    public String getDetail(String key){
        details.find{d -> d.key == key}.value
    }


    @Override
    public String toString() {
        return new StringJoiner(", ", ServiceBinding.class.getSimpleName() + "[", "]")
                .add("guid='" + guid + "'")
                .add("credentials='" + credentials + "'")
                .add("serviceInstanceId=" + serviceInstanceId)
                .add("credhubCredentialId='" + credhubCredentialId + "'")
                .toString();
    }
}
