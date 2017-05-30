package com.swisscom.cloud.sb.broker.services.common

import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.error.ErrorCode


class Utils {
    static verifyAsychronousCapableClient(ProvisionRequest provisionRequest) {
        if (!provisionRequest.acceptsIncomplete) {
            ErrorCode.ASYNC_REQUIRED.throwNew()
        }
    }
}
