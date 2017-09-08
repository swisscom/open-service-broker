package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis.service.KubernetesRedisServiceProvider
import spock.lang.IgnoreIf

import static com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup.findInternalName

@IgnoreIf({ !Boolean.valueOf(System.properties['com.swisscom.cloud.sb.broker.run3rdPartyDependentTests']) })
class KubernetesRedisFunctionalSpec extends BaseFunctionalSpec {

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('redis-kubernetes-functional-test', findInternalName(KubernetesRedisServiceProvider), 'redis')
        serviceLifeCycler.createParameter('REDIS_SERVER_MAX_MEMORY', '24', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('REDIS_MAX_MEMORY', '32', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('REDIS_MAX_CPU', '50', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('REDIS_DISK_QUOTA', '3Gi', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('MAX_CONNECTIONS', '1000', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('MAX_DATABASES', '10', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('QUORUM', '2', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('SENTINEL_MAX_CPU', '20', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('SENTINEL_MAX_MEMORY', '24', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('BACKUP_SCHEDULE_NAME', 'daily', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('BACKUP_POLICY_NAME', 'month', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('BACKUP_STORAGE_NAME', 'default', serviceLifeCycler.plan)
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