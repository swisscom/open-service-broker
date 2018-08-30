package com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.alert

import groovy.transform.CompileStatic

@CompileStatic
class AlertConfigsDto implements Serializable {
    List<AlertConfigDto> results
    int totalCount
}
