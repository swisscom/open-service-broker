package com.swisscom.cloud.sb.broker.util

import com.swisscom.cloud.sb.broker.services.AsyncServiceConfig
import com.swisscom.cloud.sb.broker.services.bosh.BoshBasedServiceConfig
import groovy.transform.CompileStatic


@CompileStatic
class DummyConfig implements BoshBasedServiceConfig, AsyncServiceConfig {
}
