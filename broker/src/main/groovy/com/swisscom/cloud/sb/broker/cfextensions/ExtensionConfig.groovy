package com.swisscom.cloud.sb.broker.cfextensions

import com.swisscom.cloud.sb.broker.config.Config

trait ExtensionConfig implements Config{
    String discoveryURL
}