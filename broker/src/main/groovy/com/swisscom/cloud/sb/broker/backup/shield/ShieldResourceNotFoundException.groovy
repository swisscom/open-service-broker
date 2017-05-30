package com.swisscom.cloud.sb.broker.backup.shield

import com.swisscom.cloud.sb.broker.error.ServiceBrokerException

class ShieldResourceNotFoundException extends ServiceBrokerException {
    ShieldResourceNotFoundException(String description) {
        super(description)
    }
}

