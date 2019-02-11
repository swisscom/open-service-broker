package com.swisscom.cloud.sb.broker.services.genericserviceprovider

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest

class CreateServiceInstanceRequestExtended extends CreateServiceInstanceRequest{

    @JsonSerialize
    @JsonProperty("service")
    Service service

    CreateServiceInstanceRequestExtended(String serviceDefinitionId, String planId,
                                         String organizationGuid, String spaceGuid,
                                         Map<String, Object> parameters, Service service){
        super(serviceDefinitionId, planId, organizationGuid, spaceGuid, parameters)


        this.service = service
    }


    @Override
    String toString() {
        return "CreateServiceInstanceRequestExtended{" +
                ", service=" + service +
                ", parameters=" + parameters +
                ", asyncAccepted=" + asyncAccepted +
                ", cfInstanceId='" + cfInstanceId + '\'' +
                ", apiInfoLocation='" + apiInfoLocation + '\'' +
                ", originatingIdentity=" + originatingIdentity +
                '}';
    }
}
