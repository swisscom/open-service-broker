package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.services.bosh.BoshFacade
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.MongoDbEnterpriseServiceProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import spock.lang.IgnoreIf

import static com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup.findInternalName

@IgnoreIf({ !Boolean.valueOf(System.properties['com.swisscom.cloud.sb.broker.run3rdPartyDependentTests']) })
class MongoDbEnterpriseFunctionalSpec extends BaseFunctionalSpec {
    @Autowired
    private ApplicationContext appContext


    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('mongodbenterprisev2', findInternalName(MongoDbEnterpriseServiceProvider), 'bosh-deployment-template-mongodb-ent')
        def plan = serviceLifeCycler.plan
        serviceLifeCycler.createParameter(BoshFacade.PLAN_PARAMETER_BOSH_VM_INSTANCE_TYPE, 'mongoenterprise-mongodbent-service', plan)
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
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

   def "deprovision mongoDbEnterprise service instance"() {
        when:
        serviceLifeCycler.deleteServiceBindingAndAssert()
        serviceLifeCycler.deleteServiceInstanceAndAssert(true, 180)
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