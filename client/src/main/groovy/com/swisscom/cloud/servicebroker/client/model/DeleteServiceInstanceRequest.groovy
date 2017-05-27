package com.swisscom.cloud.servicebroker.client.model

class DeleteServiceInstanceRequest {
    String serviceInstanceId
    String serviceId
    String planId
    boolean asyncAccepted

    DeleteServiceInstanceRequest(String serviceInstanceId, String serviceId, String planId, boolean asyncAccepted) {
        this.serviceInstanceId = serviceInstanceId
        this.serviceId = serviceId
        this.planId = planId
        this.asyncAccepted = asyncAccepted
    }
}
