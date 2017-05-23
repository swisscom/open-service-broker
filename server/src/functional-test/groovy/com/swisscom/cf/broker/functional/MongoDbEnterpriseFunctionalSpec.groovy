package com.swisscom.cf.broker.functional

import com.swisscom.cf.broker.services.bosh.BoshFacade
import com.swisscom.cf.broker.services.mongodb.enterprise.MongoDbEnterpriseServiceProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

@spock.lang.Ignore
import static com.swisscom.cf.broker.services.common.ServiceProviderLookup.findInternalName

class MongoDbEnterpriseFunctionalSpec extends BaseFunctionalSpec {
    @Autowired
    private ApplicationContext appContext


    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('mongodbenterprisev2', findInternalName(MongoDbEnterpriseServiceProvider), 'sc1-mongodbent-bosh-template')
        def plan = serviceLifeCycler.plan
        serviceLifeCycler.createParameter(BoshFacade.PLAN_PARAMETER_BOSH_VM_INSTANCE_TYPE, 'mongoenterprise-mongodbent-service', plan)
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

//    def "test binding" (){
//        given:
//        ServiceLifeCycler serviceLifeCycler = appContext.getBean('serviceLifeCycler','64ae8b79-accf-45eb-bf8e-2c2f111eea18')
//        serviceLifeCycler.createServiceIfDoesNotExist('mongodbenterprisev2', findInternalName(MongoDbEnterpriseServiceProvider), 'sc1-mongodbent-bosh-template')
//        when:
//        def result = serviceLifeCycler.bindServiceInstanceAndAssert()
//        then:
//        noExceptionThrown()
//    }

}