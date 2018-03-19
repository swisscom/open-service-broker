package com.swisscom.cloud.sb.broker.model

import com.sun.org.glassfish.gmbal.Description
import org.hibernate.validator.constraints.NotBlank

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Entity
class LastOperation extends BaseModel{
    @NotBlank
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
    String internalState

    @Override
    public String toString() {
        return "LastOperation{" +
                "id=" + id +
                ", guid='" + guid + '\'' +
                ", operation=" + operation +
                ", dateCreation=" + dateCreation +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", internalState='" + internalState + '\'' +
                '}';
    }

    static enum Status {
        IN_PROGRESS('in_progress'),
        SUCCESS('success'),
        FAILED('failed')

        final String status

        Status(String status) { this.status = status }

        @Override
        public String toString() {
            return status;
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
        public String toString() {
            return action;
        }
    }
}
