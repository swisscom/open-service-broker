package com.swisscom.cf.broker.backup.shield

import com.swisscom.cf.broker.error.ServiceBrokerException

class ShieldResourceNotFoundException extends ServiceBrokerException {
    ShieldResourceNotFoundException(String description) {
        super(description)
    }
}

