package com.swisscom.cloud.sb.broker.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.validator.constraints.NotBlank

import javax.persistence.*

@Entity
class Backup extends BaseModel{
    @NotBlank
    @Column(unique = true)
    String guid

    String serviceInstanceGuid

    @Column(columnDefinition='tinyint(1) default 0', nullable = false)
    int retryBackupCount

    @Column(unique = true)
    String externalId
    @OneToOne
    @JoinColumn(name = "service_id")
    @JsonIgnore
    CFService service
    @OneToOne
    @JoinColumn(name = "plan_id")
    @JsonIgnore
    Plan plan
    @Column(nullable = false)
    Date dateRequested

    Date dateUpdated
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    Status status
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    Operation operation

    @OneToMany
    @JoinColumn(name="backup_id")
    Set<Restore> restores = []

    @Override
    public String toString() {
        return "Backup{" +
                "id=" + id +
                ", guid='" + guid + '\'' +
                ", externalId='" + externalId + '\'' +
                ", serviceInstanceGuid='" + serviceInstanceGuid + '\'' +
                ", dateRequested=" + dateRequested +
                ", dateUpdated=" + dateUpdated +
                ", status=" + status +
                ", operation=" + operation +
                '}';
    }


    static enum Status {
        INIT('initialized', false),
        IN_PROGRESS('in_progress', false),
        SUCCESS('success', true),
        FAILED('failed', true)

        final String status
        final boolean isFinalState

        Status(String status, boolean isFinalState) { this.status = status; this.isFinalState = isFinalState }

        @Override
        public String toString() {
            return status;
        }
    }

    static enum Operation {
        CREATE('create'), DELETE('delete')

        final String operation

        Operation(String operation) { this.operation = operation }

        @Override
        public String toString() {
            return operation;
        }
    }
}
