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
import org.springframework.credhub.core.CredHubTemplate
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = "com.swisscom.cloud.sb.broker.bosh.credhub.enable", havingValue = "true")
class BoshCredHubTemplate extends CredHubTemplate {

    @Autowired
    BoshCredHubTemplate(BoshCredHubConfig config,
                        ClientHttpRequestFactory clientHttpRequestFactory,
                        ClientRegistrationRepository clientRegistrationRepository,
                        OAuth2AuthorizedClientService authorizedClientService) {
        super(config, clientHttpRequestFactory, clientRegistrationRepository, authorizedClientService)
    }

    CredHubService buildCredHubService() {
        return new CredHubServiceImpl(this)
    }
}