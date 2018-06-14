package com.swisscom.cloud.sb.broker.services.relationaldb


abstract class RelationalDbClientFactory<T extends RelationalDbClient> {
    abstract T build(String host, int port, String adminUser, String adminPassword)
}
