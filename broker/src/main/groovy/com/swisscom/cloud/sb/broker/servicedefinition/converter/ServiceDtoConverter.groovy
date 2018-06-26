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

package com.swisscom.cloud.sb.broker.servicedefinition.converter

import com.swisscom.cloud.sb.broker.cfapi.converter.CFServiceDtoConverter
import com.swisscom.cloud.sb.broker.converter.AbstractGenericConverter
import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.servicedefinition.dto.ServiceDto
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@CompileStatic
@Component
class ServiceDtoConverter extends AbstractGenericConverter<CFService, ServiceDto> {
    @Autowired
    CFServiceDtoConverter cfServiceDtoConverter

    @Autowired
    @Qualifier("ServiceDefinitionPlanDtoConverter")
    PlanDtoConverter planDtoConverter

    @Override
    protected void convert(CFService source, ServiceDto prototype) {
        cfServiceDtoConverter.convert(source, prototype)
        prototype.id = null
        prototype.guid = source.guid
        prototype.internalName = source.internalName
        prototype.displayIndex = source.displayIndex
        prototype.asyncRequired = source.asyncRequired
        prototype.plans = planDtoConverter.convertAll(source.plans)
    }
}
