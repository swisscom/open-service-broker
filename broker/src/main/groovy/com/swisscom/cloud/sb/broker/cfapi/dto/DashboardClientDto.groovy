package com.swisscom.cloud.sb.broker.cfapi.dto

import groovy.transform.CompileStatic

@CompileStatic
class DashboardClientDto implements Serializable {
    String id
    String secret
    String redirect_uri
}
