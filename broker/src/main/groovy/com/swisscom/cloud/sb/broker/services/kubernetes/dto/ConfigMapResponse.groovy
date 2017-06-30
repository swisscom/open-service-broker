package com.swisscom.cloud.sb.broker.services.kubernetes.dto

import groovy.transform.ToString

@ToString
class ConfigMapResponse implements Serializable {
    String kind
    String apiVersion
    Object data
    Object metadata
}
