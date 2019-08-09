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

import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.MongoDbEnterpriseServiceProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import spock.lang.IgnoreIf

import static com.swisscom.cloud.sb.broker.services.ServiceProviderLookup.findInternalName

@IgnoreIf({ !Boolean.valueOf(System.properties['com.swisscom.cloud.sb.broker.run3rdPartyDependentTests']) })
class MongoDbEnterpriseFunctionalSpec extends BaseFunctionalSpec {
    @Autowired
    private ApplicationContext appContext

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('mongodbenterprisev2', findInternalName(MongoDbEnterpriseServiceProvider), 'bosh-deployment-template-mongodb-ent')
        def plan = serviceLifeCycler.plan
        serviceLifeCycler.createParameter('MONGODB_VERSION', '3.6.8-ent', plan)
        serviceLifeCycler.createParameter('plan', 'mongoent.xsmall', plan)
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
        serviceLifeCycler.deleteServiceBindingAndAssert()
        serviceLifeCycler.deleteServiceInstanceAndAssert(true, 180)
    }

    def "provision mongoDbEnterprise service instance"() {
        when:
        serviceLifeCycler.createServiceInstanceAndServiceBindingAndAssert(600, true, true)
        def credentials = serviceLifeCycler.getCredentials()
        println("Credentials: ${credentials}")
        // Wait a few more seconds to let all the OpsManager operations complete before the deprovisioning requests continues
        serviceLifeCycler.pauseExecution(30)
        then:
        noExceptionThrown()
    }

    def "multiple bindings"(){
        when:
        serviceLifeCycler.bindServiceInstanceAndAssert("binding1")
        serviceLifeCycler.bindServiceInstanceAndAssert("binding2")
        serviceLifeCycler.bindServiceInstanceAndAssert("binding3")
        then:
        noExceptionThrown()
        cleanup:
        serviceLifeCycler.deleteServiceBindingAndAssert("binding1")
        serviceLifeCycler.deleteServiceBindingAndAssert("binding2")
        serviceLifeCycler.deleteServiceBindingAndAssert("binding3")
    }

   def "deprovision mongoDbEnterprise service instance"() {
        when:
        serviceLifeCycler.deleteServiceBindingAndAssert()
        serviceLifeCycler.deleteServiceInstanceAndAssert(true, 180)
        then:
        noExceptionThrown()
    }
}