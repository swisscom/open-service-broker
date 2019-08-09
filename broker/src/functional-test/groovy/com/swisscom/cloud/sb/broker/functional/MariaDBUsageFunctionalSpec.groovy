/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.broker.functional


import com.swisscom.cloud.sb.broker.services.ServiceProviderService
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
        serviceLifeCycler.createServiceIfDoesNotExist("mariadb", ServiceProviderService.findInternalName(MariaDBServiceProvider))
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