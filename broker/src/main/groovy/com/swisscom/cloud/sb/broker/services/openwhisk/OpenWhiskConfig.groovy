package com.swisscom.cloud.sb.broker.services.openwhisk

import groovy.transform.CompileStatic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@CompileStatic
@Configuration
@ConfigurationProperties(prefix = "com.swisscom.cloud.sb.broker.service.openwhisk")
class OpenWhiskConfig {
    String openWhiskUrl
    String openWhiskProtocol
    String openWhiskHost
    String openWhiskPath
    String openWhiskDbUser
    String openWhiskDbPass
    String openWhiskDbProtocol
    String openWhiskDbPort
    String openWhiskDbHost
    String openWhiskDbLocalUser
    String openWhiskDbHostname

    @Override
    public String toString() {
        return "OpenWhiskConfig{" +
                "openWhiskUrl= '" + openWhiskUrl + '\'' +
                ", openWhiskProtocol= '" + openWhiskProtocol + '\'' +
                ", openWhiskHost= '" + openWhiskHost + '\'' +
                ", openWhiskPath= '" + openWhiskPath + '\'' +
                ", openWhiskDbUser= '" + openWhiskDbUser + '\'' +
                ", openWhiskDbProtocol= '" + openWhiskDbProtocol + '\'' +
                ", openWhiskDbPort= '" + openWhiskDbPort + '\'' +
                ", openWhiskDbHost= '" + openWhiskDbHost + '\'' +
                ", openWhiskDbLocalUser= '" + openWhiskDbLocalUser + '\'' +
                ", openWhiskDbHostname= '" + openWhiskDbHostname + '\'' +
                "}"

    }
}
