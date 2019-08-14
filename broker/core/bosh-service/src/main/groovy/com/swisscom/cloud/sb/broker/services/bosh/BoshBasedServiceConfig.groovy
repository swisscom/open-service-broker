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

import com.swisscom.cloud.sb.broker.cfextensions.endpoint.EndpointConfig
import com.swisscom.cloud.sb.broker.services.bosh.resources.GenericConfig
import com.swisscom.cloud.sb.broker.services.common.TemplateConfig

// TODO: split config not needed for bosh services (e.g. portRange and EndpointConfig), this adds unnecessary config
interface BoshBasedServiceConfig extends EndpointConfig, BoshConfig {
    /**
     * PortRange should be in format:"n1-n2" where n2>n1 e.g. "27000-45000"
     * @return
     */
    String getPortRange();

    String getBoshManifestFolder();

    boolean getShuffleAzs();

    List<GenericConfig> getGenericConfigs();

    TemplateConfig getTemplateConfig();

}