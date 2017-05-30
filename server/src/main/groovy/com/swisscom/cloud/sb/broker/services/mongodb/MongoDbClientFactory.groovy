package com.swisscom.cloud.sb.broker.services.mongodb

import org.springframework.stereotype.Component

@Component
class MongoDbClientFactory {

    MongoDbClient build(String host, int port, String username, String password, String database) {
        return new MongoDbClient(username, password, host, port, database)
    }
}
