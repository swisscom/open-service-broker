package com.swisscom.cloud.sb.broker.provisioning

import groovy.transform.CompileStatic

@CompileStatic
class ProvisionResponseDto implements Serializable {
    String dashboard_url
    String operation
}
