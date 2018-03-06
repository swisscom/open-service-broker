package com.swisscom.cloud.sb.broker.util.test.DummyExtension

import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.BindResponse
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.provisioning.ProvisionResponse
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import groovy.util.logging.Slf4j
import org.apache.commons.lang.NotImplementedException
import org.springframework.stereotype.Component

@Component
@Slf4j
class DummyExtensionsServiceProvider extends DummyExtension implements ServiceProvider{

    @Override
    ProvisionResponse provision(ProvisionRequest request) {
        return new ProvisionResponse(details: [], isAsync: false)
    }

    @Override
    DeprovisionResponse deprovision(DeprovisionRequest request) {
        return new DeprovisionResponse(isAsync: false)
    }

    @Override
    BindResponse bind(BindRequest request) {
        throw new NotImplementedException()
    }

    @Override
    void unbind(UnbindRequest request) {
        throw new NotImplementedException()
    }
}
