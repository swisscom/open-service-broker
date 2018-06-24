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

package com.swisscom.cloud.sb.broker.cfapi.converter

import com.swisscom.cloud.sb.broker.cfapi.dto.PlanDto
import com.swisscom.cloud.sb.broker.cfapi.dto.SchemasDto
import com.swisscom.cloud.sb.broker.converter.AbstractGenericConverter
import com.swisscom.cloud.sb.broker.model.Plan
import groovy.transform.CompileStatic
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
        prototype.schemas = new SchemasDto(source)
    }

    private Map<String, Object> convertMetadata(Plan plan) {
        Map<String, Object> result = [:]
        plan.metadata.each { result[it.key] = getValue(it.type, it.value) }
        result
    }

}
