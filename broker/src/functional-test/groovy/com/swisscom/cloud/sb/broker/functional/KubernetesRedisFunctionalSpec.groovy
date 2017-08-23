package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis.config.KubernetesRedisConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis.service.KubernetesRedisServiceProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import spock.lang.IgnoreIf

import static com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup.findInternalName

@IgnoreIf({ !Boolean.valueOf(System.properties['com.swisscom.cloud.sb.broker.run3rdPartyDependentTests']) })
class KubernetesRedisFunctionalSpec extends BaseFunctionalSpec {

    @Autowired
    private ApplicationContext appContext
    @Autowired
    private KubernetesRedisConfig kubernetesConfig

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('redis-kubernetes', findInternalName(KubernetesRedisServiceProvider), 'redis', '1.0.0')
        def plan = serviceLifeCycler.plan
        serviceLifeCycler.createParameter('REDIS_SERVER_MAX_MEMORY', '24', plan)
        serviceLifeCycler.createParameter('REDIS_MAX_MEMORY', '32', plan)
        serviceLifeCycler.createParameter('REDIS_MAX_CPU', '50', plan)
        serviceLifeCycler.createParameter('MAX_CONNECTIONS', '1000', plan)
        serviceLifeCycler.createParameter('MAX_DATABASES', '10', plan)
        serviceLifeCycler.createParameter('QUORUM', '2', plan)
        serviceLifeCycler.createParameter('SENTINEL_MAX_CPU', '20', plan)
        serviceLifeCycler.createParameter('SENTINEL_MAX_MEMORY', '24', plan)
        serviceLifeCycler.createParameter('VERSION', '3.2.8', plan)
    }

    def "Create and remove a redis instance"() {
        when:
        try {
            serviceLifeCycler.createServiceInstanceAndServiceBindingAndAssert(300, true, true)
        }
        finally {
            serviceLifeCycler.deleteServiceBindingAndAssert()
            serviceLifeCycler.deleteServiceInstanceAndAssert(true)
            serviceLifeCycler.pauseExecution(50)
        }
        then:
        noExceptionThrown()
    }

}