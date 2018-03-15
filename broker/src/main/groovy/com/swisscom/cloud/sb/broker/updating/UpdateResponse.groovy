package com.swisscom.cloud.sb.broker.updating

import com.swisscom.cloud.sb.broker.model.ServiceDetail

class UpdateResponse {
    Collection<ServiceDetail> details
    boolean isAsync
}
