package com.swisscom.cloud.sb.broker.backup

import com.swisscom.cloud.sb.broker.BaseSpecification
import com.swisscom.cloud.sb.broker.backup.shield.ShieldTarget
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis.KubernetesRedisShieldTarget
import spock.lang.IgnoreIf

@IgnoreIf({ !Boolean.valueOf(System.properties['com.swisscom.cloud.sb.broker.run3rdPartyDependentTests']) })
class SystemBackupProviderTest extends BaseSpecification implements SystemBackupProvider {

    private final String SERVICE_INSTANCE_ID = "44651d63-b7c0-4f20-86bb-efef081a99ca"
    void setup() {

    }

    def "register and run system backup on shield"() {
        when:
        def status = configureSystemBackup(SERVICE_INSTANCE_ID)
        then:
        status.size() == 2
    }


    def "delete system backup"() {
        when:
        unregisterSystemBackupOnShield(SERVICE_INSTANCE_ID)
        then:
        noExceptionThrown()
    }

    @Override
    ShieldTarget buildShieldTarget(ServiceInstance serviceInstance) {
        return new KubernetesRedisShieldTarget(namespace: "dummy-namespace", port: 1234)
    }

    @Override
    String systemBackupJobName(String jobPrefix, String serviceInstance) {
        "SystemBackupOnShieldTest-job"
    }

    @Override
    String systemBackupTargetName(String targetPrefix, String serviceInstance) {
        "SystemBackupOnShieldTest-target"
    }

    @Override
    String shieldAgentUrl(ServiceInstance serviceInstance) {
        "localhost:1234"
    }
}
