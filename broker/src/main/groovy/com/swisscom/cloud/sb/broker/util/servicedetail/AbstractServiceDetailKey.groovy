package com.swisscom.cloud.sb.broker.util.servicedetail

import groovy.transform.CompileStatic

@CompileStatic
trait AbstractServiceDetailKey {

    private final String key
    private final ServiceDetailType serviceDetailType

    ServiceDetailType detailType() {
        return serviceDetailType
    }

    String getKey() {
        return key
    }

}
