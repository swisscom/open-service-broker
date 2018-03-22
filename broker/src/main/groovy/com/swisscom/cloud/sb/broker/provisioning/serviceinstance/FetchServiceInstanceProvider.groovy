package com.swisscom.cloud.sb.broker.provisioning.serviceinstance

interface FetchServiceInstanceProvider {
    ServiceInstanceResponseDto getServiceInstanceDetails(String instanceId)
}