package com.swisscom.cf.broker.services.common

import com.swisscom.cf.broker.binding.BindRequest
import com.swisscom.cf.broker.binding.BindResponse
import com.swisscom.cf.broker.binding.UnbindRequest
import com.swisscom.cf.broker.model.DeprovisionRequest
import com.swisscom.cf.broker.model.ProvisionRequest

interface ServiceProvider {
    ProvisionResponse provision(ProvisionRequest request)

    DeprovisionResponse deprovision(DeprovisionRequest request)

    BindResponse bind(BindRequest request)

    void unbind(UnbindRequest request)
}