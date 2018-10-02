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

import com.swisscom.cloud.sb.broker.services.credhub.CredHubService
import com.swisscom.cloud.sb.broker.services.credhub.CredHubServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.credhub.configuration.ClientHttpRequestFactoryFactory
import org.springframework.credhub.core.OAuth2CredHubTemplate
import org.springframework.credhub.support.ClientOptions
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = "com.swisscom.cloud.sb.broker.bosh.credhub.enable", havingValue = "true")
class BoshCredHubTemplate extends OAuth2CredHubTemplate {

    @Autowired
    BoshCredHubTemplate(BoshCredHubConfig config) {
        super(config.oauth2_resourceowner, config.url, new ClientHttpRequestFactoryFactory().create(new ClientOptions()))
    }

    CredHubService buildCredHubService() {
        return new CredHubServiceImpl(this)
    }
}