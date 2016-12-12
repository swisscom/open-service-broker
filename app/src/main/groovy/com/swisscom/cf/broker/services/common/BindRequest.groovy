package com.swisscom.cf.broker.services.common

import com.swisscom.cf.broker.model.Plan
import com.swisscom.cf.broker.model.ServiceInstance

class BindRequest {
    ServiceInstance serviceInstance
    String app_guid
    Plan plan
    com.swisscom.cf.broker.model.CFService service
    Map parameters
}
