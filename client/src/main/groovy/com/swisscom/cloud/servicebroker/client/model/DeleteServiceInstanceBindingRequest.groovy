package com.swisscom.cloud.servicebroker.client.model

class DeleteServiceInstanceBindingRequest {
    String serviceInstanceId
    String bindingId
    String serviceId
    String planId

    DeleteServiceInstanceBindingRequest(String serviceInstanceId, String bindingId, String serviceId, String planId) {
        this.serviceInstanceId = serviceInstanceId
        this.bindingId = bindingId
        this.serviceId = serviceId
        this.planId = planId
    }
}
