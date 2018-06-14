package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.services.mariadb.MariaDBServiceProvider
import groovy.sql.Sql
import spock.lang.IgnoreIf

@IgnoreIf({ !Boolean.valueOf(System.properties['com.swisscom.cloud.sb.broker.runMariaDBFunctionalSpec']) })
class MariaDBFunctionalSpec extends BaseFunctionalSpec {

    def setup(){
        serviceLifeCycler.createServiceIfDoesNotExist("mariadb", ServiceProviderLookup.findInternalName(MariaDBServiceProvider))
    }

    def cleanupSpec(){
        serviceLifeCycler.cleanup()
    }

    def "provision and bind MariaDB service instance"(){
        given:
        serviceLifeCycler.createServiceInstanceAndServiceBindingAndAssert()
        def credentialJson = serviceLifeCycler.getCredentials()

        when:
        def result
        Sql.withInstance(credentialJson.jdbcUrl, {
          Sql sql ->
              result = sql.execute("SELECT 1").booleanValue()
              sql.execute("CREATE TABLE new_table1 (`idnew_table` INT NULL);")
              sql.execute("DROP table new_table1;")
              result
        } )

        then:
        result
    }

    def "unbind and deprovision MariaDB service instance" (){
        expect:
        serviceLifeCycler.deleteServiceBindingAndServiceInstanceAndAssert()
    }

}