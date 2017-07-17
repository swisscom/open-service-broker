package com.swisscom.cloud.sb.broker.services.kubernetes.dto

import groovy.transform.ToString

@ToString
class RolesResponse implements Serializable {
    private String kind
    private String apiVersion
    private Object metadata
    private List<Object> rules = null
    private Map<String, Object> additionalProperties = new HashMap<String, Object>()
}
