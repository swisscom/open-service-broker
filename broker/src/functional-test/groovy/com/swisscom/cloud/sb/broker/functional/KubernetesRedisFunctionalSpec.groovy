package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.services.kubernetes.config.KubernetesConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.redis.KubernetesRedisServiceProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import spock.lang.Ignore

import static com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup.findInternalName

@Ignore
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
        for (String key : kubernetesConfig.redisPlanDefaults.keySet()) {
            serviceLifeCycler.createParameter(key, kubernetesConfig.redisPlanDefaults.get(key), plan)
        }
        for (String key : kubernetesConfig.redisConfigurationDefaults.keySet()) {
            serviceLifeCycler.createParameter(key, kubernetesConfig.redisConfigurationDefaults.get(key), plan)
        }
    }


    def "Create a redis instance"() {
        given:
        serviceLifeCycler.createServiceInstanceAndServiceBindingAndAssert(820, true, true)
    }


}

