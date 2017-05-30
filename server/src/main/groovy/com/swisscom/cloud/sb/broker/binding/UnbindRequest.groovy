package com.swisscom.cloud.sb.broker.binding

import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.model.ServiceInstance

class UnbindRequest {
    ServiceInstance serviceInstance
    ServiceBinding binding
    CFService service
}
