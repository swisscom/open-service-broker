package com.swisscom.cloud.sb.broker.services.relationaldb

import com.swisscom.cloud.sb.broker.config.Config


class RelationalDbConfig implements Config {
    String host
    String port
    String adminUser
    String adminPassword
    String databasePrefix
}
