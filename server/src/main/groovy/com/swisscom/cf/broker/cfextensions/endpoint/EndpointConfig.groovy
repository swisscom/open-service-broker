package com.swisscom.cf.broker.cfextensions.endpoint

import com.swisscom.cf.broker.config.Config

trait EndpointConfig implements Config {
    String ipRange
    String protocols
}
