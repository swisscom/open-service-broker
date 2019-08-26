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
import com.swisscom.cloud.sb.broker.cfapi.dto.jsonschema.v7.SchemaDto
import com.swisscom.cloud.sb.broker.model.Plan

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
class SchemasDto {

    @JsonSerialize
    @JsonProperty("service_instance")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    ServiceInstanceSchemaDto serviceInstanceSchema = null

    @JsonSerialize
    @JsonProperty("service_binding")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    ServiceBindingSchemaDto serviceBindingSchema = null

    SchemasDto(ServiceInstanceSchemaDto serviceInstanceSchema,
               ServiceBindingSchemaDto serviceBindingSchema) {
        this.serviceInstanceSchema = serviceInstanceSchema
        this.serviceBindingSchema = serviceBindingSchema
    }

    SchemasDto() {}

    SchemasDto(Plan plan) {
        MethodSchemaDto serviceInstanceCreateSchema = null
        if (plan.serviceInstanceCreateSchema) {
            serviceInstanceCreateSchema = new MethodSchemaDto(JsonHelper.parse(plan.serviceInstanceCreateSchema, SchemaDto) as SchemaDto)
        }
        MethodSchemaDto serviceInstanceUpdateSchema = null
        if (plan.serviceInstanceUpdateSchema) {
            serviceInstanceUpdateSchema = new MethodSchemaDto(JsonHelper.parse(plan.serviceInstanceUpdateSchema, SchemaDto) as SchemaDto)
        }
        MethodSchemaDto serviceBindingCreateSchema = null
        if (plan.serviceBindingCreateSchema) {
            serviceBindingCreateSchema = new MethodSchemaDto(JsonHelper.parse(plan.serviceBindingCreateSchema, SchemaDto) as SchemaDto)
        }

        if (serviceInstanceCreateSchema || serviceInstanceUpdateSchema) {
            serviceInstanceSchema = new ServiceInstanceSchemaDto(serviceInstanceCreateSchema, serviceInstanceUpdateSchema)
        }
        if (serviceBindingCreateSchema) {
            serviceBindingSchema = new ServiceBindingSchemaDto(serviceBindingCreateSchema)
        }
    }

}
