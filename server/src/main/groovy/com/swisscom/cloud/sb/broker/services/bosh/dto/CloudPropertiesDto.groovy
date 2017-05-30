package com.swisscom.cloud.sb.broker.services.bosh.dto

import groovy.transform.CompileStatic

@CompileStatic
class CloudPropertiesDto implements Serializable {
    String instance_type
    SchedulerHintsDto scheduler_hints
}
