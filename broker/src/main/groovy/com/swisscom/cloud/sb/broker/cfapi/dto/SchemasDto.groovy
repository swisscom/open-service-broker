package com.swisscom.cloud.sb.broker.cfapi.dto

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.util.JsonHelper

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
class SchemasDto {

    @JsonSerialize
    @JsonProperty("service_instance")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ServiceInstanceSchemaDto serviceInstanceSchema = null

    @JsonSerialize
    @JsonProperty("service_binding")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ServiceBindingSchemaDto serviceBindingSchema = null

    SchemasDto(ServiceInstanceSchemaDto serviceInstanceSchema,
               ServiceBindingSchemaDto serviceBindingSchema) {
        this.serviceInstanceSchema = serviceInstanceSchema
        this.serviceBindingSchema = serviceBindingSchema
    }

    SchemasDto(Plan plan) {
        MethodSchemaDto serviceInstanceCreateSchema = null
        if (plan.serviceInstanceCreateSchema) {
            serviceInstanceCreateSchema = new MethodSchemaDto(JsonHelper.parse(plan.serviceInstanceCreateSchema, Map) as Map)
        }
        MethodSchemaDto serviceInstanceUpdateSchema = null
        if (plan.serviceInstanceUpdateSchema) {
            serviceInstanceUpdateSchema = new MethodSchemaDto(JsonHelper.parse(plan.serviceInstanceUpdateSchema, Map) as Map)
        }
        MethodSchemaDto serviceBindingCreateSchema = null
        if (plan.serviceBindingCreateSchema) {
            serviceBindingCreateSchema = new MethodSchemaDto(JsonHelper.parse(plan.serviceBindingCreateSchema, Map) as Map)
        }

        if (serviceInstanceCreateSchema || serviceInstanceUpdateSchema) {
            serviceInstanceSchema = new ServiceInstanceSchemaDto(serviceInstanceCreateSchema, serviceInstanceUpdateSchema)
        }
        if (serviceBindingCreateSchema) {
            serviceBindingSchema = new ServiceBindingSchemaDto(serviceBindingCreateSchema)
        }
    }

}
