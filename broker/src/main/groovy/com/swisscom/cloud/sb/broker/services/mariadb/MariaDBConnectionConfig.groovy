/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

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
    String bindir
}
