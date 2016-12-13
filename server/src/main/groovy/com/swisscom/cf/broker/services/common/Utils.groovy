package com.swisscom.cf.broker.services.common

import com.swisscom.cf.broker.error.ErrorCode
import com.swisscom.cf.broker.model.ProvisionRequest


class Utils {
    static verifyAsychronousCapableClient(ProvisionRequest provisionRequest) {
        if (!provisionRequest.acceptsIncomplete) {
            ErrorCode.ASYNC_REQUIRED.throwNew()
        }
    }
}
