package com.swisscom.cloud.sb.broker.model

import javax.persistence.*

@Entity
class Restore extends BaseModel{

    @Column(nullable = false,unique = true)
    String guid
    @Column(unique = true)
    String externalId
    @Column(nullable = false)
    Date dateRequested
    Date dateUpdated
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    Backup.Status status

    @ManyToOne
    @JoinColumn(name="backup_id")
    Backup backup

    @Override
    public String toString() {
        return "Restore{" +
                "id=" + id +
                ", guid='" + guid + '\'' +
                ", externalId='" + externalId + '\'' +
                ", dateRequested=" + dateRequested +
                ", dateUpdated=" + dateUpdated +
                ", status=" + status +
                ", backup=" + backup +
                '}';
    }
}
