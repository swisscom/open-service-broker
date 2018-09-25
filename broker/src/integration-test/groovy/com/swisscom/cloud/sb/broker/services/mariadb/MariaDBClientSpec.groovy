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

import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.services.relationaldb.RelationalDbClientSpec
import com.swisscom.cloud.sb.broker.util.test.ErrorCodeHelper
import org.springframework.beans.factory.annotation.Autowired

import java.sql.SQLException

class MariaDBClientSpec extends RelationalDbClientSpec {

    @Autowired
    MariaDBConfig config

    @Autowired
    MariaDBClientFactory mariaDBClientFactory

    def setup() {
        MariaDBConnectionConfig connectionConfig = config.getDefault()
        dbClient = mariaDBClientFactory.build(connectionConfig.driver, connectionConfig.vendor, connectionConfig.host, connectionConfig.port as int, connectionConfig.adminUser, connectionConfig.adminPassword)
        initValues()
    }

    def cleanup() {
        if (dbClient != null) {
            dbClient.dropDatabase(database)
        }
    }

    def "LOCK TABLE privilege is removed on binding"() {
        given:
        dbClient.createDatabase(database)
        dbClient.createUserAndGrantRights(database, user, password, 1)
        when:
        executeStatementOnTargetDB(["""CREATE TABLE films (
                                        code        char(5),
                                        title       varchar(40),
                                        did         integer,
                                        date_prod   date,
                                        kind        varchar(10)
                                    )""",
                                    "LOCK TABLE films;"
                                    ])
        then:
        thrown(SQLException)
        cleanup:
        dbClient.revokeRightsAndDropUser(database, user)
    }


    def "happy case: get usage on empty db"() {
        given:
        dbClient.createDatabase(database)
        dbClient.databaseExists(database)
        when:
        def result = (dbClient as MariaDBClient).getUsageInBytes(database)
        then:
        noExceptionThrown()
        result == '0'
    }

    def "Throw exception if db does not exist"() {
        when:
        (dbClient as MariaDBClient).getUsageInBytes("no_such_database")
        then:
        def ex = thrown(ServiceBrokerException)
        ErrorCodeHelper.assertServiceBrokerException(ex, ErrorCode.RELATIONAL_DB_NOT_FOUND)
    }

    def "happy case: get usage on non-empty db"() {
        given:
        dbClient.createDatabase(database)
        dbClient.databaseExists(database)
        dbClient.createUserAndGrantRights(database, user, password, 10)
        executeStatementOnTargetDB(["""CREATE TABLE films (
                                        code        char(5),
                                        title       varchar(40)
                                    )""",
                                    "INSERT INTO films () VALUES('ccc', 'My Movie');"])
        when:
        def result = (dbClient as MariaDBClient).getUsageInBytes(database)
        then:
        noExceptionThrown()
        Float.parseFloat(result) > 0f
        cleanup:
        dbClient.revokeRightsAndDropUser(database, user)
    }
}

