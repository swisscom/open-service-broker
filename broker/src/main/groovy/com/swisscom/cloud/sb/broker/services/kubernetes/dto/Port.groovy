package com.swisscom.cloud.sb.broker.services.kubernetes.dto

import groovy.transform.ToString

@ToString
class Port implements Serializable {

    Integer port
    String protocol
    String name
    String targetPort
    Integer nodePort
    Map<String, Object> additionalProperties = new HashMap<String, Object>()

}
