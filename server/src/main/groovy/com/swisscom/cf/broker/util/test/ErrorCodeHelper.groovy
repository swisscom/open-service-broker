package com.swisscom.cf.broker.util.test

import com.swisscom.cf.broker.error.ErrorCode
import com.swisscom.cf.broker.error.ServiceBrokerException

class ErrorCodeHelper {
    public static boolean assertServiceBrokerException(ServiceBrokerException ex, ErrorCode errorCode) {
        ex.code == errorCode.code && ex.error_code == errorCode.errorCode
    }
}
