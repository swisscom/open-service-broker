package com.swisscom.cloud.sb.broker.services.kubernetes.dto

import groovy.transform.ToString

@ToString
class Spec implements Serializable {

    String type
    List<Port> ports = null
    Selector selector
    Map<String, Object> additionalProperties = new HashMap<String, Object>()
}
