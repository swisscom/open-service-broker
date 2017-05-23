package com.swisscom.cf.broker.binding

import com.swisscom.cf.broker.model.ServiceBinding
import com.swisscom.cf.broker.model.ServiceInstance

class UnbindRequest {
    ServiceInstance serviceInstance
    ServiceBinding binding
    com.swisscom.cf.broker.model.CFService service
}
