package com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.alert

import groovy.transform.CompileStatic

@CompileStatic
class AlertNotification implements Serializable {
    int delayMin
    String emailAddress
    int intervalMin
    String typeName
}
