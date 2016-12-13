package com.swisscom.cf.broker.functional

import com.swisscom.cf.broker.services.bosh.BoshFacade
import com.swisscom.cf.broker.services.mongodb.enterprise.v2.MongoDbEnterpriseV2ServiceProvider
import spock.lang.Ignore
import spock.lang.IgnoreIf

import static com.swisscom.cf.broker.services.common.ServiceProviderLookup.findInternalName

@Ignore
class MongoDbEnterpriseV2FunctionalSpec extends BaseFunctionalSpec {

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('mongodbenterprisev2', findInternalName(MongoDbEnterpriseV2ServiceProvider), 'sc1-mongodbent-bosh-template')
        def plan = serviceLifeCycler.plan
        serviceLifeCycler.createParameter(BoshFacade.PARAM_BOSH_VM_INSTANCE_TYPE, 'm1.small', plan)
        serviceLifeCycler.createParameter('plan', 'mongoent.small', plan)
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "provision mongoDbEnterprise service instance"() {
        when:
        serviceLifeCycler.createServiceInstanceAndServiceBindingAndAssert(820, true, true)
        def credentials = serviceLifeCycler.getCredentials()
        println("Credentials: ${credentials}")
        then:
        noExceptionThrown()
    }

    def "deprovision mongoDbEnterprise service instance"() {
        when:
        serviceLifeCycler.deleteServiceBindingAndAssert()
        serviceLifeCycler.deleteServiceInstanceAndAssert(true)
        serviceLifeCycler.pauseExecution(400)

        then:
        noExceptionThrown()
    }

}