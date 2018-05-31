package com.swisscom.cloud.sb.broker.util.test

import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.BindResponse
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.UpdateRequest
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.provisioning.ProvisionResponse
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import com.swisscom.cloud.sb.broker.services.health.ServiceHealthProvider
import com.swisscom.cloud.sb.broker.updating.UpdateResponse
import com.swisscom.cloud.sb.model.health.ServiceHealth
import com.swisscom.cloud.sb.model.health.ServiceHealthStatus
import org.springframework.stereotype.Component

@Component
class DummyServiceHealthServiceProvider implements ServiceProvider, ServiceHealthProvider  {
    @Override
    ProvisionResponse provision(ProvisionRequest request) {
        return new ProvisionResponse(details: [], isAsync: false)
    }

    @Override
    DeprovisionResponse deprovision(DeprovisionRequest request) {
        return new DeprovisionResponse(details: [], isAsync: false)
    }

    @Override
    BindResponse bind(BindRequest request) {
        return null
    }

    @Override
    void unbind(UnbindRequest request) {

    }

    @Override
    UpdateResponse update(UpdateRequest request) {
        return null
    }

    @Override
    ServiceHealth getHealth(ServiceInstance serviceInstance) {
        new ServiceHealth( status: ServiceHealthStatus.OK)
    }
}
