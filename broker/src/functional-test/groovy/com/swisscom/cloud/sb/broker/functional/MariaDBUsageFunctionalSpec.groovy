package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.services.mariadb.MariaDBServiceProvider
import com.swisscom.cloud.sb.model.usage.ServiceUsage
import com.swisscom.cloud.sb.model.usage.ServiceUsageType
import groovy.sql.Sql
import groovy.util.logging.Log4j
import org.springframework.http.ResponseEntity
import spock.lang.IgnoreIf

@Log4j
@IgnoreIf({ !Boolean.valueOf(System.properties['com.swisscom.cloud.sb.broker.runMariaDBUsageFunctionalSpec']) })
class MariaDBUsageFunctionalSpec extends BaseFunctionalSpec {

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist("mariadb", ServiceProviderLookup.findInternalName(MariaDBServiceProvider))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "Get usage for MariaDB instance"() {
        given:
        serviceLifeCycler.createServiceInstanceAndServiceBindingAndAssert()
        def credentialJson = serviceLifeCycler.getCredentials()
        def resultAfterCreation
        Sql.withInstance(credentialJson.jdbcUrl, {
            Sql sql ->
                sql.execute("CREATE TABLE new_table1 (`idnew_table` INT NULL);")
                sql.execute("INSERT INTO new_table1 VALUES(1);")
                resultAfterCreation = sql.firstRow("SELECT idnew_table FROM new_table1").idnew_table
        })
        assert resultAfterCreation == 1

        when:
        String instance = serviceLifeCycler.getServiceInstanceId()
        ResponseEntity<ServiceUsage> response = serviceBrokerClient.getUsage(instance)
        then:
        response.getStatusCodeValue() == 200
        Float.parseFloat(response.getBody().getValue()) > 0f
        response.getBody().getType() == ServiceUsageType.WATERMARK
    }


    def "unbind and deprovision MariaDB service instance"() {
        expect:
        serviceLifeCycler.deleteServiceBindingAndServiceInstanceAndAssert()
    }

}