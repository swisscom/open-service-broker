package com.swisscom.cloud.sb.broker.services.relationaldb

import com.swisscom.cloud.sb.broker.BaseTransactionalSpecification
import groovy.sql.Sql

import java.sql.SQLException

import static com.swisscom.cloud.sb.broker.util.StringGenerator.randomAlphaNumericOfLength16

abstract class RelationalDbClientSpec extends BaseTransactionalSpecification {
    protected RelationalDbClient dbClient
    protected String database
    protected String user
    protected String password


    void initValues() {
        database = randomAlphaNumericOfLength16()
        user = randomAlphaNumericOfLength16()
        password = randomAlphaNumericOfLength16()
    }

    def "databaseExists return false for non existent db"() {
        expect:
        !dbClient.databaseExists(database)
        cleanup:
        dbClient = null
    }

    def "database creation succeeds"() {
        when:
        dbClient.createDatabase(database)
        then:
        dbClient.databaseExists(database)
    }


    def "database creation when a db with the same name already exists"() {
        given:
        dbClient.createDatabase(database)
        when:
        dbClient.createDatabase(database)
        then:
        thrown SQLException
    }

    def "happy case: drop database"() {
        given:
        dbClient.createDatabase(database)
        when:
        dbClient.dropDatabase(database)
        then:
        noExceptionThrown()
        !dbClient.databaseExists(database)
    }

    def "happy case: create user and grant him the access rights"() {
        given:
        dbClient.createDatabase(database)
        when:
        dbClient.createUserAndGrantRights(database, user, password, 1)
        executeStatementOnTargetDB(["SELECT 1",
                                    """CREATE TABLE films (
                                        code        char(5),
                                        title       varchar(40),
                                        did         integer,
                                        date_prod   date,
                                        kind        varchar(10)
                                    )"""])
        then:
        noExceptionThrown()

        cleanup:
        dbClient.revokeRightsAndDropUser(database, user)
    }

    def "user creation fails when it aldready exists"() {
        given:
        dbClient.createDatabase(database)
        dbClient.createUserAndGrantRights(database, user, password, 0)
        when:
        dbClient.createUserAndGrantRights(database, user, password, 0)
        then:
        thrown SQLException
        cleanup:
        dbClient.revokeRightsAndDropUser(database, user)
    }

    def "happy case: drop user"() {
        given:
        dbClient.createDatabase(database)
        dbClient.createUserAndGrantRights(database, user, password, 0)
        dbClient.revokeRightsAndDropUser(database, user)

        when:
        executeStatementOnTargetDB(["SELECT 1"]) //This action should fail if the user is dropped

        then:
        thrown SQLException
    }

    def "happy case: user exists"() {
        given:
        dbClient.createDatabase(database)
        dbClient.createUserAndGrantRights(database, user, password, 0)
        expect:
        dbClient.userExists(user)
        cleanup:
        dbClient.revokeRightsAndDropUser(database, user)
    }

    protected def executeStatementOnTargetDB(List<String> statements) {
        try {
            Sql.withInstance(generateJdbcUrl(), user, password,
                    {
                        Sql conn -> (dbSpecificStatements() + statements).each { String statement -> conn.execute(statement) }
                    })
        } catch (Exception e) {
            e.printStackTrace()
            throw e
        }
    }

    protected String generateJdbcUrl() {
        return dbClient.getJdbcUrlWithoutDatabase() + "/" + database
    }

    List<String> dbSpecificStatements() {
        return []
    }
}
