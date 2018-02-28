package com.swisscom.cloud.sb.broker.services.common

import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.BindResponse
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.UpdateRequest
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.provisioning.ProvisionResponse
import com.swisscom.cloud.sb.broker.updating.UpdateResponse

interface ServiceProvider {
    ProvisionResponse provision(ProvisionRequest request)

    DeprovisionResponse deprovision(DeprovisionRequest request)

    BindResponse bind(BindRequest request)

    void unbind(UnbindRequest request)

    UpdateResponse update(UpdateRequest request)
}