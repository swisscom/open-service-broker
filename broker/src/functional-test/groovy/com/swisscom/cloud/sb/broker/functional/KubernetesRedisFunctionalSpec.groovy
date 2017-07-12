package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis.config.KubernetesRedisConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis.service.KubernetesRedisServiceProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

import static com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup.findInternalName

class KubernetesRedisFunctionalSpec extends BaseFunctionalSpec {


    @Autowired
    private ApplicationContext appContext
    @Autowired
    private KubernetesRedisConfig kubernetesConfig

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('kubernetesRedisService', findInternalName(KubernetesRedisServiceProvider))
        createPlanParameters(serviceLifeCycler.plan)
    }

    private void createPlanParameters(Plan plan) {
        for (String key : kubernetesConfig.redisPlanDefaults.keySet()) {
            serviceLifeCycler.createParameter(key, kubernetesConfig.redisPlanDefaults.get(key), plan)
        }
    }


    def "Create a redis instance"() {
        given:
        serviceLifeCycler.createServiceInstanceAndServiceBindingAndAssert(40, true, true)
    }


}

