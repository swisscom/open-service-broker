package com.swisscom.cloud.sb.broker.services.mariadb

import com.swisscom.cloud.sb.broker.backup.shield.ShieldServiceConfig
import com.swisscom.cloud.sb.broker.cfextensions.extensions.ExtensionConfig
import com.swisscom.cloud.sb.broker.services.relationaldb.RelationalDbConfig
import groovy.transform.AutoClone
import groovy.transform.CompileStatic
import org.springframework.context.annotation.Configuration

@AutoClone
@CompileStatic
@Configuration
class MariaDBConnectionConfig extends RelationalDbConfig implements ShieldServiceConfig, ExtensionConfig {
    /**
     * Use this overwrite to inject the correct port
     * when a backup got configured from a functional test,
     * so that the OSBE database can kept on port 3306 and
     * the functional test can use SSH-forwarded Galera DB
     * to test the MariaDB service functionality.
     */
    String overwriteGaleraPortForShieldTesting
    String name
}
