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

package com.swisscom.cloud.sb.broker.services.relationaldb

import groovy.sql.Sql
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

import java.sql.SQLException

@Log4j
@CompileStatic
abstract class RelationalDbClient {
    private final String driverClass
    private final String vendor
    private final String host
    private final int port
    private final String adminUser
    private final String adminPassword
    private final String database

    RelationalDbClient(String driverClass, String vendor, String host, int port, String adminUser, String adminPassword) {
        this(driverClass, vendor, host, port, adminUser, adminPassword, null)
    }

    RelationalDbClient(String driverClass, String vendor, String host, int port, String adminUser, String adminPassword, String database) {
        this.driverClass = driverClass
        this.vendor = vendor
        this.host = host
        this.port = port
        this.adminUser = adminUser
        this.adminPassword = adminPassword
        this.database = database
    }

    String getJdbcUrl() {
        return getJdbcUrlWithoutDatabase() + (database ? "/${database}" : "")
    }

    String getJdbcUrlWithoutDatabase() {
        return "jdbc:${vendor}://${host}:${port}"
    }

    void createDatabase(String database) {
        log.info("Creating database: ${database}")
        handleSql { Sql sql -> this.&handleCreateDatabase(sql, database) }
    }


    protected abstract void handleCreateDatabase(Sql sql, String database)

    Boolean databaseExists(String database) {
        log.info("Searching for database: ${database}")
        boolean exists = false
        handleSql { Sql sql ->
            exists = this.&handleDatabaseExists(sql, database)
            return
        }
        return exists
    }

    protected abstract boolean handleDatabaseExists(Sql sql, String database)

    void dropDatabase(String database) {
        log.info("Drop database: ${database}")
        handleSql { Sql sql -> this.&handleDropDatabase(sql, database) }
    }


    protected abstract void handleDropDatabase(Sql sql, String database)

    Boolean userExists(String user) {
        log.info("Search for user: ${user}")
        Boolean exists = false
        handleSql { Sql sql ->
            exists = this.&handleUserExists(sql, user)
            return
        }
        return exists
    }

    protected abstract boolean handleUserExists(Sql sql, String user)

    RelationalDbBindResponseDto createUserAndGrantRights(String database, String user, String password, int maxConnections) {
        log.info("Create user: ${user} on database:${database} and grant rights")
        handleSql { Sql sql -> this.&handleCreateUserAndGrantRights(sql, database, user, password, maxConnections) }
        return generateCredentials(database, user, password)
    }


    protected RelationalDbBindResponseDto generateCredentials(String database, String user, String password) {
        return new RelationalDbBindResponseDto(vendor: vendor, host: host, port: port, username: user, password: password, database: database)
    }

    protected
    abstract void handleCreateUserAndGrantRights(Sql sql, String database, String user, String password, int maxConnections)

    void revokeRightsAndDropUser(String database, String user) {
        log.info("Revoke privileges for database: ${database} for user: ${user}")
        handleSql { Sql sql -> this.&handleRevokeRightsAndDropUser(sql, database, user) }
    }

    protected abstract void handleRevokeRightsAndDropUser(Sql sql, String database, String user)

    protected void handleSql(Closure<Sql> closure) {
        try {
            Class.forName(driverClass)
            Sql.withInstance(getJdbcUrl(), adminUser, adminPassword, driverClass, closure)
        } catch (SQLException e) {
            log.info("Error: ${e.message}")
            throw e
        }
    }
}
