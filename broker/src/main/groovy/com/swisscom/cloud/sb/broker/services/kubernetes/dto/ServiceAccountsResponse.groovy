package com.swisscom.cloud.sb.broker.services.kubernetes.dto

import groovy.transform.ToString

@ToString
class ServiceAccountsResponse implements Serializable {
    String kind
    String apiVersion
    Object metadata
    Map<String, Object> additionalProperties = new HashMap<String, Object>()
}
