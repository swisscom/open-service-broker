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
class PlanMetadata extends BaseModel{

    @Column(name = '_key')
    String key
    @Column(name = '_value')
    String value
    @Column(name = '_type',columnDefinition="varchar(255) default 'String'")
    String type

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="plan_id")
    @JsonIgnore
    Plan plan

    static mapping = {
        key column: '_key'
        value column: '_value', type: "text"
        type column: '_type', defaultValue: "'String'"
    }
}
