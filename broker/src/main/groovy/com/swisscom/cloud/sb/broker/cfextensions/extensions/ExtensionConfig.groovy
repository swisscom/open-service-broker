package com.swisscom.cloud.sb.broker.cfextensions.extensions

import com.swisscom.cloud.sb.broker.config.Config

trait ExtensionConfig implements Config {
    String discoveryURL
    String serverURL
    Map<String, Map<String, String>> credentials
    String adheresTo
}