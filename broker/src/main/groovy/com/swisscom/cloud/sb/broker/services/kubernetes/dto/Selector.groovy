package com.swisscom.cloud.sb.broker.services.kubernetes.dto

import groovy.transform.ToString

@ToString
class Selector implements Serializable {

    String instances
    String role
    Map<String, Object> additionalProperties = new HashMap<String, Object>()
}
