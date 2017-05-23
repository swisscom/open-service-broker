package com.swisscom.cf.broker.services.mongodb.enterprise

import com.swisscom.cf.broker.services.common.HostPort

class MongoDbEnterpriseDeployment {
    String database
    String replicaSet
    List<HostPort> hostPorts
    String monitoringAgentUser
    String monitoringAgentPassword
    String backupAgentUser
    String backupAgentPassword
    String operationsUser
    String operationsPassword
    String healthUser
    String healthPassword
}
