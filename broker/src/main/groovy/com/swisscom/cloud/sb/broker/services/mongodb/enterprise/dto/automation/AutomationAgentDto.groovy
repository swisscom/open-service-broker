package com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.automation

import groovy.transform.CompileStatic

@CompileStatic
class AutomationAgentDto implements Serializable {
    int confCount
    String hostname
    String stateName
    String typeName
}
