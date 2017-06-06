package com.swisscom.cloud.sb.broker.util.test

import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException

class ErrorCodeHelper {
    public static boolean assertServiceBrokerException(ServiceBrokerException ex, ErrorCode errorCode) {
        ex.code == errorCode.code && ex.error_code == errorCode.errorCode
    }
}
