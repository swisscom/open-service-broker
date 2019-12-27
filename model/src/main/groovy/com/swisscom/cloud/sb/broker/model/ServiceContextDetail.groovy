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
import com.google.common.base.Preconditions

import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
class ServiceContextDetail extends BaseModel {

    @Column(name = '_key')
    @NotNull
    private String key
    @Column(name = '_value')
    @NotNull
    private String value

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_context_id")
    @JsonIgnore
    private ServiceContext serviceContext

    /**
     * Public no-arg constructor is needed for JPA compliance for more infos check
     * <a href="https://en.wikipedia.org/wiki/Java_Persistence_API">here</a>
     */
    ServiceContextDetail() {}

    private ServiceContextDetail(String key, String value) {
        this(key, value, null);
    }

    private ServiceContextDetail(String key, String value, ServiceContext serviceContext) {
        Preconditions.checkNotNull(key, "Key is not allowed to be null for ServiceContextDetail")
        Preconditions.checkNotNull(key, "Value is not allowed to be null for ServiceContextDetail")
        this.key = key
        this.value = value
        this.serviceContext = serviceContext
    }

    static ServiceContextDetail of(String key, String value, ServiceContext serviceContext) {
        return new ServiceContextDetail(key, value, serviceContext)
    }

    static ServiceContextDetail of(String key, String value) {
        return new ServiceContextDetail(key, value)
    }

    String getKey() {
        return key
    }

    void setKey(String key) {
        this.key = key
    }

    String getValue() {
        return value
    }

    void setValue(String value) {
        this.value = value
    }

    ServiceContext getServiceContext() {
        return serviceContext
    }

    void setServiceContext(ServiceContext serviceContext) {
        this.serviceContext = serviceContext
    }

    @Override
    public String toString() {
        return new StringJoiner(" ", ServiceContextDetail.class.getSimpleName() + "[", "]")
                .add("key='" + key + "'")
                .add("value='" + value + "'")
                .toString();
    }

    boolean equals(o) {
        if (this.is(o)) {
            return true
        }
        if (getClass() != o.class) {
            return false
        }

        ServiceContextDetail that = (ServiceContextDetail) o

        if (key != that.key) {
            return false
        }
        if (value != that.value) {
            return false
        }

        return true
    }

    int hashCode() {
        int result
        result = key.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}
