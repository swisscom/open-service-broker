package com.swisscom.cf.broker.util.test

import com.swisscom.cf.broker.exception.ErrorCode
import com.swisscom.cf.broker.exception.ServiceBrokerException

class ErrorCodeHelper {
    public static boolean assertServiceBrokerException(ServiceBrokerException ex, ErrorCode errorCode) {
        ex.code == errorCode.code && ex.error_code == errorCode.errorCode
    }
}
