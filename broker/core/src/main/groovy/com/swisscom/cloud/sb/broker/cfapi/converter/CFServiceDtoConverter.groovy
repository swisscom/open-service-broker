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

import com.swisscom.cloud.sb.broker.cfapi.dto.CFServiceDto
import com.swisscom.cloud.sb.broker.converter.AbstractGenericConverter
import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.CFServicePermission
import com.swisscom.cloud.sb.broker.model.Tag
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@CompileStatic
@Component
class CFServiceDtoConverter extends AbstractGenericConverter<CFService, CFServiceDto> {
    @Autowired
    protected PlanDtoConverter planDtoConverter

    @Autowired
    protected DashboardClientDtoConverter dashboardClientDtoConverter

    @Override
    void convert(CFService source, CFServiceDto prototype) {
        prototype.id = source.guid
        prototype.name = source.name
        prototype.description = source.description
        prototype.metadata = convertMetadata(source)
        prototype.plans = planDtoConverter.convertAll(source.plans.sort { it.displayIndex })
        prototype.tags = source.tags.collect { Tag t -> t.tag }
        prototype.requires = source.permissions.collect { CFServicePermission p -> p.permission }
        prototype.dashboard_client = dashboardClientDtoConverter.convert(source)

        prototype.bindable = source.bindable
        prototype.active = source.active
        prototype.plan_updateable = source.plan_updateable
        prototype.bindingsRetrievable = source.bindingsRetrievable
        prototype.instancesRetrievable = source.instancesRetrievable
    }

    private Map<String, Object> convertMetadata(CFService service) {
        Map<String, Object> result = [:]
        service.metadata.each { result[it.key] = MetadataJsonHelper.getValue(it.type, it.value) }
        result
    }
}
