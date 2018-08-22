package com.swisscom.cloud.sb.broker.model

interface RequestWithParameters {
    String serviceInstanceGuid
    Plan plan
    String parameters
}
