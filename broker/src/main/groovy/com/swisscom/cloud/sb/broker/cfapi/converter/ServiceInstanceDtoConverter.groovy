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

import com.swisscom.cloud.sb.broker.converter.AbstractGenericConverter
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.provisioning.serviceinstance.ServiceInstanceResponseDto
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

@CompileStatic
@Component
class ServiceInstanceDtoConverter extends AbstractGenericConverter<ServiceInstance, ServiceInstanceResponseDto> {

    @Override
    void convert(ServiceInstance source, ServiceInstanceResponseDto prototype) {
        prototype.serviceId = source.plan.service.guid
        prototype.planId = source.plan.guid
        prototype.dashboardUrl = null
        prototype.parameters = source.parameters
        prototype.parentServiceInstance = source.parentServiceInstance?.guid
        prototype.childInstances = source.childs.collect( {it -> it.guid })
    }
}