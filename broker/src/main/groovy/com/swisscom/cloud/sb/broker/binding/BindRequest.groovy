package com.swisscom.cloud.sb.broker.binding

import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ServiceInstance

class BindRequest {
    ServiceInstance serviceInstance
    // Question: should binding_guid rather be of type ServiceBinding?
    String binding_guid
    String app_guid
    Plan plan
    CFService service
    Map parameters
}
