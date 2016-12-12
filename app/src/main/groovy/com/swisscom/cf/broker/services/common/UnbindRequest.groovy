package com.swisscom.cf.broker.services.common

import com.swisscom.cf.broker.model.ServiceBinding
import com.swisscom.cf.broker.model.ServiceInstance

class UnbindRequest {
    ServiceInstance serviceInstance
    ServiceBinding binding
    com.swisscom.cf.broker.model.CFService service
}
