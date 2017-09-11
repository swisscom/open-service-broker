package com.swisscom.cloud.sb.broker.backup.shield

import com.swisscom.cloud.sb.broker.config.BackupServiceConfig

class ShieldServiceConfig implements BackupServiceConfig {
    String storeName
    String retentionName
    String scheduleName
}
