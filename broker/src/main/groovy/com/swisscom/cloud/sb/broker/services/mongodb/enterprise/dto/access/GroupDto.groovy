package com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.access


class GroupDto implements Serializable {

    String id
    String name
    String activeAgentCount
    String replicaSetCount
    String shardCount
    boolean publicApiEnabled
    String agentApiKey
    transient HostCounts hostCounts

    static class HostCounts implements Serializable {
        int arbiter
        int config
        int primary
        int secondary
        int mongos
        int master
        int slave
    }
}
