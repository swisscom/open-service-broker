package com.swisscom.cloud.sb.broker.util.test

import com.swisscom.cloud.sb.broker.util.ParentServiceProvider
import org.springframework.stereotype.Component

@Component
class ParentDummyServiceProvider extends DummyServiceProvider implements ParentServiceProvider {
}
