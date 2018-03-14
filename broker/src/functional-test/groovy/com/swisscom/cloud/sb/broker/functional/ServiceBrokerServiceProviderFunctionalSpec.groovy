package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.config.UserConfig
import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Parameter
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.services.genericserviceprovider.ServiceBrokerServiceProvider
import com.swisscom.cloud.sb.broker.util.ServiceLifeCycler
import com.swisscom.cloud.sb.broker.util.test.DummyServiceProvider
import com.swisscom.cloud.sb.client.model.LastOperationState
import jdk.nashorn.internal.ir.annotations.Ignore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.client.HttpClientErrorException
import spock.lang.Shared

import static com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup.findInternalName
@Ignore
class ServiceBrokerServiceProviderFunctionalSpec extends BaseFunctionalSpec {

    private final String BASE_URL = "baseUrl"
    private final String USERNAME = "username"
    private final String PASSWORD = "password"
    private final String SERVICE_INSTANCE_ID = "service-guid"
    private final String PLAN_ID = "plan-guid"

    private CFService cfService
    private Plan plan


    @Autowired
    ServiceLifeCycler proxyServiceLifeCycler

    @Shared
    ServiceLifeCycler sharedServiceLifeCycler

    def setup() {
        def planId = UUID.randomUUID().toString()

        serviceLifeCycler.createParameter(BASE_URL,"http://localhost:8080", serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(USERNAME, cfAdminUser.username, serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(PASSWORD,cfAdminUser.password, serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(SERVICE_INSTANCE_ID,UUID.randomUUID().toString(), serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(PLAN_ID,planId, serviceLifeCycler.plan)

        plan = new Plan(guid: planId, internalName: findInternalName(ServiceBrokerServiceProvider), asyncRequired: false)
        serviceLifeCycler.createServiceIfDoesNotExist('dummyServiceProvider', findInternalName(ServiceBrokerServiceProvider), null, null, null, 0, plan)
        serviceLifeCycler.setParameters()
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "provision sync service instance"() {
        given:
        //Plan plan = getServicePlan(cfService)

        when:
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false)
        //serviceLifeCycler.createServiceInstanceAndAssert(maxSecondsToAwaitInstance: 0, asyncRequest: false, asyncResponse: false, service: cfService, servicePlan: plan)

        then:
        noExceptionThrown()
    }

    Plan getServicePlan(CFService cfService1) {
        Iterator<Plan> it = cfService1.plans.iterator()
        while (it.hasNext()) {
            def plan = it.next()
            plan.internalName = "serviceBrokerServiceProvider"
            return plan
        }
    }

    def "provision async service instance"() {
        given:
        // add +30s because of the startup delay of the quartz scheduler
        serviceLifeCycler.createServiceInstanceAndAssert(maxSecondsToAwaitInstance: DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS + 30, asyncRequest: true, asyncResponse: true, parameters: ['success': false])
        assert serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.FAILED
        when:
        def response = serviceLifeCycler.requestBindService('someKindaServiceInstanceGuid')
        then:
        def ex = thrown(HttpClientErrorException)
        ex.rawStatusCode == 412
    }

    def "deprovision async service instance"() {
        when:
        serviceLifeCycler.deleteServiceInstanceAndAssert(true, 35)
        then:
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED
    }
}
