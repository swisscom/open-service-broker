package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.services.kubernetes.redis.KubernetesRedisServiceProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import spock.lang.Ignore

import static com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup.findInternalName

class KubernetesRedisFunctionalSpec extends BaseFunctionalSpec {


    @Autowired
    private ApplicationContext appContext

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('kubernetesRedisService', findInternalName(KubernetesRedisServiceProvider))

    }

    def "Create a namespace"() {
        given:
        serviceLifeCycler.createServiceInstanceAndServiceBindingAndAssert(820, true, true)
    }


}

