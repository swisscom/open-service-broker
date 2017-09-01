package com.swisscom.cloud.sb.broker.util

import org.springframework.stereotype.Component

@Component
class RestTemplateBuilderFactory {
    RestTemplateBuilder build() { return new RestTemplateBuilder() }
}