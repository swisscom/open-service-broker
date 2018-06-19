package com.swisscom.cloud.sb.broker.binding

import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ServiceInstance

class BindRequest {
    ServiceInstance serviceInstance
    String binding_guid
    String app_guid
    Plan plan
    CFService service
    Map parameters
}
