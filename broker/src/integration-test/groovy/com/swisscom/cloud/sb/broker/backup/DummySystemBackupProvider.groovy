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

import com.swisscom.cloud.sb.broker.backup.shield.ShieldTarget
import com.swisscom.cloud.sb.broker.cfextensions.extensions.Extension
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import groovy.json.JsonGenerator
import org.springframework.stereotype.Service

@Service
class DummySystemBackupProvider implements SystemBackupProvider {
    private static final String SHIELD_TEST = "TEST-SYSTEM-BACKUP-PROVIDER"
    private static final String SHIELD_AGENT_URL = "127.0.0.1:5444"

    @Override
    ShieldTarget buildShieldTarget(ServiceInstance serviceInstance) {
        return new ShieldTarget() {
            @Override
            String pluginName() {
                return "dummy"
            }

            @Override
            String endpointJson() {
                new JsonGenerator.Options().excludeNulls().build().toJson(
                        [data: SHIELD_TEST])
            }
        }
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
        SHIELD_AGENT_URL
    }

    Collection<Extension> buildExtensions() {

    }
}
