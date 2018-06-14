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
