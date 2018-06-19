package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.services.mariadb.MariaDBServiceProvider
import groovy.sql.Sql
import spock.lang.IgnoreIf

@IgnoreIf({ !Boolean.valueOf(System.properties['com.swisscom.cloud.sb.broker.runMariaDBAsynchronousFunctionalSpec']) })
class MariaDBAsynchronousFunctionalSpec extends BaseFunctionalSpec {

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist("mariadb", ServiceProviderLookup.findInternalName(MariaDBServiceProvider))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "provision and bind MariaDB service instance"() {
        given:
        serviceLifeCycler.createServiceInstanceAndServiceBindingAndAssert(0, true, false)
        def credentialJson = serviceLifeCycler.getCredentials()

        when:
        def result
        Sql.withInstance(credentialJson.jdbcUrl, {
            Sql sql ->
                result = sql.execute("SELECT 1").booleanValue()
        })

        then:
        result
    }

    def "unbind and deprovision MariaDB service instance"() {
        expect:
        serviceLifeCycler.deleteServiceBindingAndServiceInstanceAndAssert()
    }

}