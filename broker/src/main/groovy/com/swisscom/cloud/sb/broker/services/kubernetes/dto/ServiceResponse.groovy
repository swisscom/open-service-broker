package com.swisscom.cloud.sb.broker.services.kubernetes.dto

import groovy.transform.ToString

@ToString
class ServiceResponse implements Serializable {
    String kind
    String apiVersion
    Spec spec
    Object status
    Object metadata
}
