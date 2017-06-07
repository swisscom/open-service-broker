package com.swisscom.cloud.sb.broker.cfextensions.endpoint

import com.swisscom.cloud.sb.broker.config.Config

trait EndpointConfig implements Config {
    List<String> ipRanges
    List<String> protocols
}
