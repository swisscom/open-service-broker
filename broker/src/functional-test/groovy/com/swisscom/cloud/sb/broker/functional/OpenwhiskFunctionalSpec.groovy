package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.services.openwhisk.OpenwhiskServiceProvider
import static com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup.findInternalName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
//import spock.lang.IgnoreIf
//
//
//@IgnoreIf({ !Boolean.valueOf(System.properties['com.swisscom.cloud.sb.broker.run3rdPartyDependentTests']) })
class OpenwhiskFunctionalSpec extends BaseFunctionalSpec {
    @Autowired
    private ApplicationContext appContext

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('openwhiskTest', findInternalName(OpenwhiskServiceProvider))
        def plan = serviceLifeCycler.plan
        serviceLifeCycler.createParameter("openwhiskName1", "openwhiskValue", plan)
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "provision openwhisk service instance"() {
        when:
            serviceLifeCycler.createServiceInstanceAndServiceBindingAndAssert(1)
            def credentials = serviceLifeCycler.getCredentials()
            println("Credentials: ${credentials}")
        then:
            noExceptionThrown()
    }

    def "deprovision openwhisk service instance"() {
        when:
            serviceLifeCycler.deleteServiceBindingAndAssert()
            serviceLifeCycler.deleteServiceInstanceAndAssert()
            serviceLifeCycler.pauseExecution(1)

        then:
            noExceptionThrown()
    }
}
