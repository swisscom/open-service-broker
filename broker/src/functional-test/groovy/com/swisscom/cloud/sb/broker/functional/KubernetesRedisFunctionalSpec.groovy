package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.services.kubernetes.config.KubernetesConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.redis.KubernetesRedisServiceProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

import static com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup.findInternalName

class KubernetesRedisFunctionalSpec extends BaseFunctionalSpec {


    @Autowired
    private ApplicationContext appContext
    @Autowired
    private KubernetesConfig kubernetesConfig

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('kubernetesRedisService', findInternalName(KubernetesRedisServiceProvider))
        createPlanParameters(serviceLifeCycler.plan)
    }

    private void createPlanParameters(Plan plan) {
        for (String key : kubernetesConfig.planParameters.keySet()) {
            serviceLifeCycler.createParameter(key, kubernetesConfig.planParameters.get(key), plan)
        }
        for (String key : kubernetesConfig.configurationParameters.keySet()) {
            serviceLifeCycler.createParameter(key, kubernetesConfig.configurationParameters.get(key), plan)
        }
    }


    def "Create a namespace"() {
        given:
        serviceLifeCycler.createServiceInstanceAndServiceBindingAndAssert(820, true, true)
    }


}

