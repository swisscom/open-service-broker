package com.swisscom.cloud.sb.broker.services.genericserviceprovider

import com.swisscom.cloud.sb.broker.BaseSpecification
import com.swisscom.cloud.sb.broker.config.ApplicationUserConfig
import com.swisscom.cloud.sb.broker.config.UserConfig
import com.swisscom.cloud.sb.broker.config.WebSecurityConfig
import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Parameter
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.services.lapi.config.LapiConfig
import org.springframework.beans.factory.annotation.Autowired

class ServiceBrokerServiceProviderUsageTest extends BaseSpecification {

    private ApplicationUserConfig applicationUserConfig
    private ServiceBrokerServiceProvider serviceBrokerServiceProvider
    private ServiceBrokerServiceProviderUsage serviceBrokerServiceProviderUsage
    private String SERVICE_INSTANCE_GUID = "65d546f1-2c74-4871-9d5f-b5b0df1a8912"
    private String SERVICE_DEFINITION_GUID = "65d546f1-2c74-4871-9d5f-b5b0df1a8913"
    private String PLAN_GUID = "65d546f1-2c74-4871-9d5f-b5b0df1a7083"
    private String BASE_URL = "http://localhost:4567"

    private Plan syncPlan
    private CFService service

    @Autowired
    private LapiConfig lapiConfig

    def setup() {
        applicationUserConfig = new ApplicationUserConfig()
        applicationUserConfig.platformUsers = new ArrayList<UserConfig>()
        def userConfig = new UserConfig(username: lapiConfig.lapiUsername, platformId: "new-id", password: lapiConfig.lapiPassword, role: WebSecurityConfig.ROLE_CF_EXT_ADMIN)
        applicationUserConfig.platformUsers.add(userConfig)
        serviceBrokerServiceProviderUsage = new ServiceBrokerServiceProviderUsage(applicationUserConfig)
        serviceBrokerServiceProvider = new ServiceBrokerServiceProvider(serviceBrokerServiceProviderUsage)
        syncPlan = new Plan(guid: "65d546f1-2c74-4871-9d5f-b5b0df1a8920", asyncRequired: false, parameters: [new Parameter(name: "baseUrl", value: BASE_URL), new Parameter(name: "username", value: lapiConfig.lapiUsername), new Parameter(name: "password", value: lapiConfig.lapiPassword), new Parameter(name: "service-guid", value: SERVICE_DEFINITION_GUID), new Parameter(name: "plan-guid", value: PLAN_GUID)])
        service = new CFService(guid: "65d546f1-2c74-4871-9d5f-b5b0df1a8914")
    }

    def "request usage"() {
        given:
        ServiceInstance serviceInstance= new ServiceInstance(guid: SERVICE_INSTANCE_GUID, plan: syncPlan)

        when:
        serviceBrokerServiceProvider.findUsage(serviceInstance, null)

        then:
        noExceptionThrown()
    }
}
