package com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.automation


class ReplicaSetDto implements Serializable {
    String _id
    List<Member> members

    static class Member implements Serializable {
        int _id
        boolean arbiterOnly
        boolean hidden
        String host
        int priority
        int votes
        int slaveDelay
    }
}
