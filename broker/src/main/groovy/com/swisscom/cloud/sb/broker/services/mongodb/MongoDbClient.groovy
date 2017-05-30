package com.swisscom.cloud.sb.broker.services.mongodb
import com.mongodb.MongoCredential
import com.mongodb.MongoTimeoutException
import com.mongodb.ServerAddress

import static com.swisscom.cloud.sb.broker.error.ErrorCode.MONGODB_NOT_READY_YET
import static com.swisscom.cloud.sb.broker.util.StringGenerator.randomAlphaNumericOfLength16

class MongoDbClient {
    public static final String ADMIN_DATABASE = "admin"

    private final String username
    private final String password
    private final String host
    private final int port
    private final String database

    MongoDbClient(String username, String password, String host, int port, String database) {
        this.username = username
        this.password = password
        this.host = host
        this.port = port
        this.database = database
    }

    private def connect() {
        MongoCredential credential = MongoCredential.createScramSha1Credential(username, ADMIN_DATABASE, password as char[]);
        return new com.mongodb.MongoClient(new ServerAddress(host, port), Arrays.asList(credential));
    }

    MongoDbBindResponseDto createDatabaseAndUser() {
        com.mongodb.MongoClient mongo = null
        try {
            mongo = connect()
            def db = mongo.getDB(database);
            String newUsername = randomAlphaNumericOfLength16()
            String newPassword = randomAlphaNumericOfLength16()
            db.addUser(newUsername, newPassword as char[])

            return new MongoDbBindResponseDto(database: database, username: newUsername, password: newPassword, hosts: [this.host], port: this.port)
        } catch (MongoTimeoutException ex) {
            MONGODB_NOT_READY_YET.throwNew()
        }
        finally {
            if (mongo) {
                mongo.close()
            }
        }
    }

    void deleteUser(String username) {
        com.mongodb.MongoClient mongo = null
        try {
            mongo = connect()
            def db = mongo.getDB(database)
            db.removeUser(username)
        } finally {
            if (mongo) {
                mongo.close()
            }
        }
    }
}