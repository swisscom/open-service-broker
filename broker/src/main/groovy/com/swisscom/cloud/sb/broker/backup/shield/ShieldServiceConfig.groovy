package com.swisscom.cloud.sb.broker.backup.shield

import com.swisscom.cloud.sb.broker.config.Config

class ShieldServiceConfig implements Config {
    String storeName
    String retentionName
    String scheduleName
}
