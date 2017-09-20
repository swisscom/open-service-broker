package com.swisscom.cloud.sb.broker.backup.shield

import com.swisscom.cloud.sb.broker.config.Config

trait ShieldServiceConfig implements Config {
    String shieldAgentUrl // can be a fixed agent path or a dynamic with is generted from ServiceDetails
}
