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

import com.swisscom.cloud.sb.broker.backup.shield.ShieldTarget
import groovy.json.JsonGenerator

/*
 Example:
 {
   "uuid":"8710b942-4ccd-437e-8f55-de1d984b25aa",
   "name":"doesnmatter",
   "summary":"arbitrary",
   "plugin":"mysql", // this is pluginName()
   "endpoint":"{\n  \"mysql_user\": \"root\",\n  \"mysql_password\": \"\",\n  \"mysql_host\": \"127.0.0.1\",\n  \"mysql_port\": \"3306\"\n}", // here goes the endpointJson()
   "agent":"10.244.2.2:5444"
 }
 */

class MariaDBShieldTarget implements ShieldTarget {
    public static final String MYSQL_OPTIONS = "--single-transaction --routines --events"

    String user
    String password
    String host
    String database
    String port // shield needs a string
    String bindir

    @Override
    String pluginName() {
        "mysql"
    }

    @Override
    String endpointJson() {
        new JsonGenerator.Options().excludeNulls().build().toJson(
                [mysql_user    : user,
                 mysql_password: password,
                 mysql_host    : host,
                 mysql_port    : port,
                 mysql_database: database,
                 mysql_options : MYSQL_OPTIONS,
                 mysql_bindir  : bindir])
    }
}
