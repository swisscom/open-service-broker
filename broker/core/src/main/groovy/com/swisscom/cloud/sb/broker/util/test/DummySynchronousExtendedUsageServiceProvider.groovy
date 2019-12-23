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

package com.swisscom.cloud.sb.broker.util.test


import com.google.gson.Gson
import com.swisscom.cloud.sb.broker.services.credential.BindRequest
import com.swisscom.cloud.sb.broker.services.credential.BindResponse
import com.swisscom.cloud.sb.broker.services.credential.BindResponseDto
import com.swisscom.cloud.sb.broker.services.credential.UnbindRequest
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.UpdateRequest
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.provisioning.ProvisionResponse
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import com.swisscom.cloud.sb.broker.services.usage.ExtendedServiceUsageProvider
import com.swisscom.cloud.sb.broker.updating.UpdateResponse
import com.swisscom.cloud.sb.model.usage.ServiceUsageType
import com.swisscom.cloud.sb.model.usage.extended.ServiceUsageItem
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component

@Component
@Slf4j
@CompileStatic
class DummySynchronousExtendedUsageServiceProvider implements ServiceProvider, ExtendedServiceUsageProvider {
    @Override
    BindResponse bind(BindRequest request) {
        log.warn("Bind parameters: ${request.parameters?.toString()}")
        return new BindResponse(credentials: new BindResponseDto() {
            @Override
            String toJson() {
                request.parameters ? new Gson().toJson(request.parameters) : '{}'
            }
        })
    }

    @Override
    void unbind(UnbindRequest request) {

    }

    @Override
    UpdateResponse update(UpdateRequest request) {
        ErrorCode.SERVICE_UPDATE_NOT_ALLOWED.throwNew()
        return null
    }

    @Override
    ProvisionResponse provision(ProvisionRequest request) {
        return new ProvisionResponse(details: [], isAsync: false)
    }

    @Override
    DeprovisionResponse deprovision(DeprovisionRequest request) {
        return new DeprovisionResponse(isAsync: false)
    }

    @Override
    Set<ServiceUsageItem> getUsages(ServiceInstance serviceInstance) {
        return [
                new ServiceUsageItem(
                        property: "cpu",
                        value: 1.0f,
                        unit: "number",
                        type: ServiceUsageType.WATERMARK
                ),
                new ServiceUsageItem(
                        property: "ram",
                        value: 1.0f,
                        unit: "gb",
                        type: ServiceUsageType.WATERMARK
                )
        ] as Set
    }
}
