package com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.automation

import groovy.transform.CompileStatic

@CompileStatic
class AutomationStatusDto implements Serializable {
    int goalVersion
    Collection<Process> processes

    static class Process {
        String hostname
        int lastGoalVersionAchieved
        String name
        Collection<String> plan
    }
}
