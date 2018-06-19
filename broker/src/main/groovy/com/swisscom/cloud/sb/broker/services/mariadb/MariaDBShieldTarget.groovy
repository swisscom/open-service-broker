package com.swisscom.cloud.sb.broker.services.mariadb

import com.swisscom.cloud.sb.broker.backup.shield.ShieldTarget
import groovy.json.JsonBuilder

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

    @Override
    String pluginName() {
        "mysql"
    }

    @Override
    String endpointJson() {
        new JsonBuilder(mysql_user: user,
                mysql_password: password,
                mysql_host: host,
                mysql_port: port,
                mysql_database: database,
                mysql_options: MYSQL_OPTIONS)
    }
}
