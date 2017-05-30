package com.swisscom.cloud.sb.broker.services.common

import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.ProvisionRequest

class Utils {
    static verifyAsychronousCapableClient(ProvisionRequest provisionRequest) {
        if (!provisionRequest.acceptsIncomplete) {
            ErrorCode.ASYNC_REQUIRED.throwNew()
        }
    }
}
