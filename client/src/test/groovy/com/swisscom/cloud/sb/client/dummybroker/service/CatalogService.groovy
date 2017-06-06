package com.swisscom.cloud.sb.client.dummybroker.service

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileStatic
import org.springframework.cloud.servicebroker.model.Catalog
import org.springframework.cloud.servicebroker.model.ServiceDefinition
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct

@Service
@CompileStatic
class CatalogService implements org.springframework.cloud.servicebroker.service.CatalogService {
    private Catalog catalog

    @PostConstruct
    public void init(){
        catalog = new ObjectMapper().readValue(new File(this.getClass().getResource('/demo-service-definition.json').getFile()).text, Catalog.class)
    }

    @Override
    Catalog getCatalog() {
        return  catalog
    }

    @Override
    ServiceDefinition getServiceDefinition(String serviceId) {
        return catalog.serviceDefinitions.find {it.id == serviceId}
    }
}
