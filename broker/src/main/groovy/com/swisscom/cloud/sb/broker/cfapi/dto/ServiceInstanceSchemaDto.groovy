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

package com.swisscom.cloud.sb.broker.cfapi.dto

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
class ServiceInstanceSchemaDto {

    @JsonSerialize
    @JsonProperty("create")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private MethodSchemaDto createMethodSchema = null

    @JsonSerialize
    @JsonProperty("update")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private MethodSchemaDto updateMethodSchema = null

    ServiceInstanceSchemaDto(MethodSchemaDto createMethodSchema,
                             MethodSchemaDto updateMethodSchema) {
        this.createMethodSchema = createMethodSchema
        this.updateMethodSchema = updateMethodSchema
    }
}
