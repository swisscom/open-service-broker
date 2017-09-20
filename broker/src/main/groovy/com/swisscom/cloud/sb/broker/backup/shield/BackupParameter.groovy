package com.swisscom.cloud.sb.broker.backup.shield

import com.swisscom.cloud.sb.broker.config.BackupServiceConfig

class BackupParameter implements BackupServiceConfig {
    String storeName
    String retentionName
    String scheduleName
    String agent
}
