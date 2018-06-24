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