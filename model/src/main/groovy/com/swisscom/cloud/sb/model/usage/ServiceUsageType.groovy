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

package com.swisscom.cloud.sb.model.usage

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum ServiceUsageType {
    TRANSACTIONS("transactions"),
    WATERMARK("watermark"),

    final String type

    ServiceUsageType(final String type) {
        this.type = type
    }

    String toString() {
        type
    }

    @JsonValue
    public String getValue() {
        return type
    }

    @JsonCreator
    public static ServiceUsageType fromString(String key) {
        return key ? ServiceUsageType.values().find { it.value == key } :  null
    }
}
