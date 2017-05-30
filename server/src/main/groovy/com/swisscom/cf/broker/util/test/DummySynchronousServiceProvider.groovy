package com.swisscom.cf.broker.util.test

import com.google.common.base.Optional
import com.google.gson.Gson
import com.swisscom.cf.broker.binding.BindRequest
import com.swisscom.cf.broker.binding.BindResponse
import com.swisscom.cf.broker.binding.BindResponseDto
import com.swisscom.cf.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.model.usage.ServiceUsage
import com.swisscom.cf.broker.cfextensions.serviceusage.ServiceUsageProvider
import com.swisscom.cf.broker.model.DeprovisionRequest
import com.swisscom.cf.broker.model.ProvisionRequest
import com.swisscom.cf.broker.model.ServiceInstance
import com.swisscom.cf.broker.provisioning.DeprovisionResponse
import com.swisscom.cf.broker.provisioning.ProvisionResponse
import com.swisscom.cf.broker.services.common.*
import com.swisscom.cloud.sb.model.usage.ServiceUsageType
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component

@Component
@Slf4j
@CompileStatic
class DummySynchronousServiceProvider implements ServiceProvider, ServiceUsageProvider {
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
    ProvisionResponse provision(ProvisionRequest request) {
        return new ProvisionResponse(details: [], isAsync: false)
    }

    @Override
    DeprovisionResponse deprovision(DeprovisionRequest request) {
        return new DeprovisionResponse(isAsync: false)
    }

    @Override
    ServiceUsage findUsage(ServiceInstance serviceInstance, Optional<Date> enddate) {
        Date date = enddate.present ? enddate.get() : new Date()
        return new ServiceUsage(type: ServiceUsageType.TRANSACTIONS, value: "${date.time}", enddate: date)
    }
}
