package com.swisscom.cloud.sb.broker

import com.swisscom.cloud.sb.broker.services.AsyncServiceConfig
import org.springframework.stereotype.Component

@Component
class DummyAsyncServiceConfig implements AsyncServiceConfig {
    List<String> ipRanges = ["127.0.0.1"]
    List<String> protocols = ["tcp"]
    int retryIntervalInSeconds = 30
    int maxRetryDurationInMinutes = 30
}
