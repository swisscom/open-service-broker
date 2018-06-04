package com.swisscom.cloud.sb.broker.util.servicedetail

import groovy.transform.CompileStatic

@CompileStatic
trait AbstractServiceDetailKey {

    private String key
    private ServiceDetailType serviceDetailType

    ServiceDetailType detailType() {
        return serviceDetailType
    }

    String getKey() {
        return key
    }

}
