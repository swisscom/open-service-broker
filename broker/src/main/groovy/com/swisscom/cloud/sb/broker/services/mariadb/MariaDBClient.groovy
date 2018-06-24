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
import com.swisscom.cloud.sb.broker.services.relationaldb.RelationalDbClient
import groovy.sql.Sql
import groovy.util.logging.Log4j

@Log4j
class MariaDBClient extends RelationalDbClient {
    public static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver"
    public static final String VENDOR_MARIADB = "mysql"
    public static final String MYSQL_PORT = "3306"

    MariaDBClient(String host, int port, String adminUser, String adminPassword) {
        //TODO should the vendor be mysql or mariadb?
        super(MYSQL_DRIVER, VENDOR_MARIADB, host, port, adminUser, adminPassword)
    }

    @Override
    protected void handleCreateDatabase(Sql sql, String database) {
        String query = "CREATE DATABASE ${database}"
        sql.execute(query)
    }

    @Override
    protected boolean handleDatabaseExists(Sql sql, String database) {
        String query = "SELECT COUNT(*) SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '${database}'"
        return ((int) sql.firstRow(query).get("SCHEMA_NAME")) > 0
    }

    @Override
    protected void handleDropDatabase(Sql sql, String database) {
        String query = "DROP DATABASE IF EXISTS ${database}"
        sql.execute(query)
    }

    @Override
    protected boolean handleUserExists(Sql conn, String user) {
        String query = "SELECT COUNT(*) AS COUNT FROM mysql.user WHERE user='${user}'"
        return ((int) conn.firstRow(query).get("COUNT")) > 0
    }

    @Override
    protected void handleCreateUserAndGrantRights(Sql conn, String database, String user, String password, int maxConnections) {
        conn.execute("CREATE USER ?@'%' IDENTIFIED BY ?", [user, password])
        handleGrant(conn, database, user, maxConnections)
    }

    private void handleGrant(Sql conn, String database, String user, int maxConnections) {
        conn.execute("GRANT ALL PRIVILEGES ON ${database}.* TO ?@'%'", [user])
        // revoke LOCK TABLES privilege as it's not properly supported by Galera (CLOUDAC-5459)
        conn.execute("REVOKE LOCK TABLES ON ${database}.* FROM ?@'%'", [user])

        if (maxConnections > 0) {
            log.info("GRANT USAGE for database: ${database} for user: ${user} max_user_connections: ${maxConnections}")
            conn.execute("GRANT USAGE ON ${database}.* TO ?@'%' WITH MAX_USER_CONNECTIONS ?", [user, maxConnections])
        }
    }

    @Override
    protected void handleRevokeRightsAndDropUser(Sql conn, String database, String user) {
        conn.execute("DROP USER ${user}@'%'")
    }

    String getUsageInBytes(String database) {
        if (!databaseExists(database)) {
            ErrorCode.RELATIONAL_DB_NOT_FOUND.throwNew()
        }
        String dbSizeInBytes
        handleSql { Sql conn ->
            dbSizeInBytes = conn.firstRow("SELECT sum(data_length + index_length) " +
                    "FROM information_schema.TABLES WHERE table_schema =  '${database}'")[0]
        }
        return dbSizeInBytes ? dbSizeInBytes : '0'
    }

}
