package com.swisscom.cloud.sb.client.model

import groovy.transform.CompileStatic

@CompileStatic
class ProvisionResponseDto implements Serializable {
    String dashboard_url
    Collection<Extension> extension_apis
}
