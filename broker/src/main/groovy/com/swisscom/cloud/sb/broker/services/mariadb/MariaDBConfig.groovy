package com.swisscom.cloud.sb.broker.services.mariadb

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = 'com.swisscom.cloud.sb.broker.service.mariadb')
class MariaDBConfig {
    String nameOfDefault
    MariaDBConnectionConfig[] clusters

    MariaDBConnectionConfig getDefault() {
        return nameOfDefault ? getByName(nameOfDefault) : clusters.first()
    }

    MariaDBConnectionConfig getByName(String name) {
        clusters.find { c -> c.name == name }
    }
}
