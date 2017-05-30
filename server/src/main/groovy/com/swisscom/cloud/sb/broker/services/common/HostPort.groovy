package com.swisscom.cloud.sb.broker.services.common

import groovy.transform.CompileStatic

@CompileStatic
public class HostPort {
    String host
    int port

    static HostPort from(String uri) {
        return new HostPort(host: uri.substring(0, uri.indexOf(':')), port: uri.substring(uri.indexOf(':') + 1) as int)
    }
}