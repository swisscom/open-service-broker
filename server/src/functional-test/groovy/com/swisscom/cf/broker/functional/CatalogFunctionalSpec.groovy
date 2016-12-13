package com.swisscom.cf.broker.functional

import org.springframework.cloud.servicebroker.model.Catalog
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class CatalogFunctionalSpec extends BaseFunctionalSpec {
    public static final String serviceName = 'Supercalifragilisticexpialidocious ' + new Random().nextInt()

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist(serviceName, null)
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def 'catalog dto is created correctly'() {
        when:
        ResponseEntity<Catalog> response = serviceBrokerClient.getCatalog()
        then:
        response.statusCode == HttpStatus.OK
        println(response.body.toString())
        response.body.serviceDefinitions.size() > 0
        def newAddedService = response.body.serviceDefinitions.find { it.name == serviceName }
        newAddedService.tags.size() == 1
        newAddedService.plans.size() == 1
    }
}