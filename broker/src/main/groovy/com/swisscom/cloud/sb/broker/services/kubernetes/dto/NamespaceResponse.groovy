package com.swisscom.cloud.sb.broker.services.kubernetes.dto

import groovy.transform.ToString

@ToString
class NamespaceResponse implements Serializable {
    String kind
    String apiVersion
    Object metadata
    Object spec
    Object status
    Map<String, Object> additionalProperties = new HashMap<String, Object>()
}
