package com.swisscom.cloud.sb.broker.util.test

import com.google.common.base.Optional
import com.google.gson.Gson
import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.BindResponse
import com.swisscom.cloud.sb.broker.binding.BindResponseDto
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.cfextensions.extensions.Extension
import com.swisscom.cloud.sb.broker.cfextensions.serviceusage.ServiceUsageProvider
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
import com.swisscom.cloud.sb.model.usage.ServiceUsage
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
                        value: "1",
                        unit: "number",
                        type: ServiceUsageType.WATERMARK
                ),
                new ServiceUsageItem(
                        property: "ram",
                        value: "1",
                        unit: "gb",
                        type: ServiceUsageType.WATERMARK
                )
        ] as Set
    }
}
