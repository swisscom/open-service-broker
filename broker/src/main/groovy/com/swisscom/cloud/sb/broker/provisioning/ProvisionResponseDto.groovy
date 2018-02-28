package com.swisscom.cloud.sb.broker.provisioning

import com.swisscom.cloud.sb.broker.cfextensions.extensions.Extension
import groovy.transform.CompileStatic

@CompileStatic
class ProvisionResponseDto implements Serializable {
    String dashboard_url
    Collection<Extension> extension_apis
}
