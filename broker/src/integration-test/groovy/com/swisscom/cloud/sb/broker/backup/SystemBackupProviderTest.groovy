/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.broker.backup

import com.swisscom.cloud.sb.broker.BaseSpecification
import com.swisscom.cloud.sb.broker.backup.shield.ShieldTarget
import com.swisscom.cloud.sb.broker.cfextensions.extensions.Extension
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

    Collection<Extension> buildExtensions(){

    }
}
