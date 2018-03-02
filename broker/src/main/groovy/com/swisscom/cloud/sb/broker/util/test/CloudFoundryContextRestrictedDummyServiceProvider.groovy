package com.swisscom.cloud.sb.broker.util.test

import com.swisscom.cloud.sb.broker.context.CloudFoundryContextRestrictedOnly
import org.springframework.stereotype.Component

@Component
class CloudFoundryContextRestrictedDummyServiceProvider extends DummyServiceProvider implements CloudFoundryContextRestrictedOnly {
}
