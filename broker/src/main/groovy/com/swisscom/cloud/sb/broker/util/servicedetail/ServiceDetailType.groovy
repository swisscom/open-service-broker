package com.swisscom.cloud.sb.broker.util.servicedetail

import groovy.transform.CompileStatic

@CompileStatic
enum ServiceDetailType {

    HOST("host"),
    PORT("port"),
    USERNAME("username"),
    PASSWORD("password"),
    OTHER("other")

    private final String type

    ServiceDetailType(String type) {
        this.type = type
    }

    String getType() {
        return type
    }
}
