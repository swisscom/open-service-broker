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

package com.swisscom.cloud.sb.broker.services.bosh

import com.swisscom.cloud.sb.broker.services.AsyncServiceConfigImpl
import com.swisscom.cloud.sb.broker.services.bosh.resources.GenericConfig
import com.swisscom.cloud.sb.broker.services.common.TemplateConfig

class DummyConfig extends AsyncServiceConfigImpl implements BoshBasedServiceConfig {
    int retryIntervalInSeconds

    int maxRetryDurationInMinutes

    List<String> ipRanges

    List<String> protocols

    String portRange

    String boshManifestFolder

    boolean shuffleAzs

    List<GenericConfig> genericConfigs

    String boshDirectorBaseUrl

    String boshDirectorUsername

    String boshDirectorPassword

    @Override
    TemplateConfig getTemplateConfig() {
        return TemplateConfig.EMPTY
    }
}
