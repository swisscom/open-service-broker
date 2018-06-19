package com.swisscom.cloud.sb.broker.services.mariadb

import com.swisscom.cloud.sb.broker.services.relationaldb.RelationalDbClientFactory
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

@CompileStatic
@Component
class MariaDBClientFactory extends RelationalDbClientFactory<MariaDBClient> {

    MariaDBClient build(String host, int port, String username, String password) {
        return new MariaDBClient(host, port, username, password)
    }
}
