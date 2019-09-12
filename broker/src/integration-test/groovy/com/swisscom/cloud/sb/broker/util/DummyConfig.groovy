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

package com.swisscom.cloud.sb.broker.util

import com.swisscom.cloud.sb.broker.services.AsyncServiceConfig
import com.swisscom.cloud.sb.broker.services.bosh.BoshBasedServiceConfig
import com.swisscom.cloud.sb.broker.services.bosh.resources.GenericConfig
import com.swisscom.cloud.sb.broker.services.common.TemplateConfig
import groovy.transform.CompileStatic

@CompileStatic
class DummyConfig implements BoshBasedServiceConfig, AsyncServiceConfig {
    @Override
    String getPortRange() {
        return null
    }

    @Override
    String getBoshManifestFolder() {
        return null
    }

    @Override
    List<GenericConfig> getGenericConfigs() {
        return null
    }

    @Override
    TemplateConfig getTemplateConfig() {
        return null
    }

    @Override
    List<String> getIpRanges() {
        return null
    }

    @Override
    List<String> getProtocols() {
        return null
    }

    @Override
    String getBoshDirectorBaseUrl() {
        return null
    }

    @Override
    String getBoshDirectorUsername() {
        return null
    }

    @Override
    String getBoshDirectorPassword() {
        return null
    }
}
