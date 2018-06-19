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
        dbClient = mariaDBClientFactory.build(connectionConfig.host, connectionConfig.port as int, connectionConfig.adminUser, connectionConfig.adminPassword)
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

