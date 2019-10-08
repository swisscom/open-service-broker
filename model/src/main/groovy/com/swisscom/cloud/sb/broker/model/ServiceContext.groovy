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

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.validation.constraints.NotNull

@Entity
class ServiceContext extends BaseModel {

    @NotNull
    String platform

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_context_id")
    Set<ServiceContextDetail> details = []

    @Override
    public String toString() {
        return new StringJoiner(" ", ServiceContext.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("platform='" + platform + "'")
                .add("details=" + details)
                .toString();
    }

    boolean equals(o) {
        if (this.is(o)) {
            return true
        }
        if (!(o instanceof ServiceContext)) {
            return false
        }

        ServiceContext that = (ServiceContext) o

        if (details != that.details) {
            return false
        }
        if (platform != that.platform) {
            return false
        }

        return true
    }

    int hashCode() {
        int result
        result = (platform != null ? platform.hashCode() : 0)
        result = 31 * result + (details != null ? details.hashCode() : 0)
        return result
    }
}
