package com.swisscom.cloud.sb.broker.cfapi.converter

import com.swisscom.cloud.sb.broker.cfapi.dto.PlanDto
import com.swisscom.cloud.sb.broker.converter.AbstractGenericConverter
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.util.JsonHelper
import groovy.transform.CompileStatic
import org.springframework.cloud.servicebroker.model.MethodSchema
import org.springframework.cloud.servicebroker.model.Schemas
import org.springframework.cloud.servicebroker.model.ServiceBindingSchema
import org.springframework.cloud.servicebroker.model.ServiceInstanceSchema
import org.springframework.stereotype.Component

import static MetadataJsonHelper.getValue

@Component
@CompileStatic
class PlanDtoConverter extends AbstractGenericConverter<Plan, PlanDto> {

    @Override
    void convert(Plan source, PlanDto prototype) {
        prototype.id = source.guid
        prototype.name = source.name
        prototype.description = source.description
        prototype.free = source.free
        prototype.metadata = convertMetadata(source)
        prototype.schemas = convertSchemas(source)
    }

    private Map<String, Object> convertMetadata(Plan plan) {
        Map<String, Object> result = [:]
        plan.metadata.each { result[it.key] = getValue(it.type, it.value) }
        result
    }

    private Schemas convertSchemas(Plan plan) {
        MethodSchema serviceInstanceCreateSchema = null
        if (plan.serviceInstanceCreateSchema) {
            serviceInstanceCreateSchema = new MethodSchema(JsonHelper.parse(plan.serviceInstanceCreateSchema, Map) as Map)
        }
        MethodSchema serviceInstanceUpdateSchema = null
        if (plan.serviceInstanceUpdateSchema) {
            serviceInstanceUpdateSchema = new MethodSchema(JsonHelper.parse(plan.serviceInstanceUpdateSchema, Map) as Map)
        }
        MethodSchema serviceBindingCreateSchema = null
        if (plan.serviceBindingCreateSchema) {
            serviceBindingCreateSchema = new MethodSchema(JsonHelper.parse(plan.serviceBindingCreateSchema, Map) as Map)
        }

        ServiceInstanceSchema serviceInstanceSchema = null
        if (serviceInstanceCreateSchema || serviceInstanceUpdateSchema) {
            serviceInstanceSchema = new ServiceInstanceSchema(serviceInstanceCreateSchema, serviceInstanceUpdateSchema)
        }
        ServiceBindingSchema serviceBindingSchema = null
        if (serviceBindingCreateSchema) {
            serviceBindingSchema = new ServiceBindingSchema(serviceBindingCreateSchema)
        }

        Schemas schemas = null
        if (serviceInstanceSchema || serviceBindingSchema) {
            schemas = new Schemas(serviceInstanceSchema, serviceBindingSchema)
        }
        schemas
    }
}
