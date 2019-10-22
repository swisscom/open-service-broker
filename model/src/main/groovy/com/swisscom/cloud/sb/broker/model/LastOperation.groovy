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

import com.sun.org.glassfish.gmbal.Description

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotBlank

@Entity
class LastOperation extends BaseModel{
    @Column(unique = true)
    @Description("ServiceInstanceGuid")
    String guid

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Operation operation
    @Column(nullable = false)
    Date dateCreation

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Status status

    String description
    @Description("Statemachine State")
    String internalState

    @Override
    String toString() {
        return "LastOperation{" +
                "id=" + id +
                ", guid='" + guid + '\'' +
                ", operation=" + operation +
                ", dateCreation=" + dateCreation +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", internalState='" + internalState + '\'' +
                '}'
    }

    static enum Status {
        IN_PROGRESS('in_progress'),
        SUCCESS('success'),
        FAILED('failed')

        final String status

        Status(String status) { this.status = status }

        @Override
        String toString() {
            return status
        }
    }

    static enum Operation {
        PROVISION("provision"),
        DEPROVISION("deprovision"),
        UPDATE("update")

        final String action

        Operation(String action) {
            this.action = action
        }

        @Override
        String toString() {
            return action
        }
    }

    boolean equals(o) {
        if (this.is(o)) {
            return true
        }
        if (!(o instanceof LastOperation)) {
            return false
        }

        LastOperation that = (LastOperation) o

        if (dateCreation != that.dateCreation) {
            return false
        }
        if (description != that.description) {
            return false
        }
        if (guid != that.guid) {
            return false
        }
        if (internalState != that.internalState) {
            return false
        }
        if (operation != that.operation) {
            return false
        }
        if (status != that.status) {
            return false
        }

        return true
    }

    int hashCode() {
        int result
        result = guid.hashCode()
        result = 31 * result + (operation != null ? operation.hashCode() : 0)
        result = 31 * result + (dateCreation != null ? dateCreation.hashCode() : 0)
        result = 31 * result + (status != null ? status.hashCode() : 0)
        result = 31 * result + (description != null ? description.hashCode() : 0)
        result = 31 * result + (internalState != null ? internalState.hashCode() : 0)
        return result
    }
}
