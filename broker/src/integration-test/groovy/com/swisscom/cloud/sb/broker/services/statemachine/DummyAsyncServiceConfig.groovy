package com.swisscom.cloud.sb.broker.services.statemachine

import com.swisscom.cloud.sb.broker.services.AsyncServiceConfig
import org.springframework.stereotype.Component

@Component
class DummyAsyncServiceConfig implements AsyncServiceConfig {
    List<String> ipRanges
    List<String> protocols
}
