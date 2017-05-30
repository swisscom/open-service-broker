package com.swisscom.cloud.sb.broker.binding

import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ServiceInstance

class BindRequest {
    ServiceInstance serviceInstance
    String app_guid
    Plan plan
    CFService service
    Map parameters
}
