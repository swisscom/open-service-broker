package com.swisscom.cf.broker.services.mongodb.enterprise.dto.automation

class ClusterDto implements Serializable {
    String id
    String typeName
    String clusterName
    String shardName
    String replicaSetName
}
