package com.swisscom.cloud.sb.broker.services.kubernetes.dto

import groovy.transform.ToString

@ToString
class DeploymentResponse implements Serializable {
    String kind
    String apiVersion
    Object spec
    Object status
    Object metadata
}
