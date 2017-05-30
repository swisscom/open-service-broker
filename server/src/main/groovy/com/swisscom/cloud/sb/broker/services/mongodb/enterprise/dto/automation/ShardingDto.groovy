package com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.automation


class ShardingDto implements Serializable {
    String name
    List<String> configServer
    List<Collection> collections
    List<Shard> shards

    static class Collection implements Serializable {
        String _id
        String key
    }

    static class Shard implements Serializable {
        String _id
        String rs
    }
}
