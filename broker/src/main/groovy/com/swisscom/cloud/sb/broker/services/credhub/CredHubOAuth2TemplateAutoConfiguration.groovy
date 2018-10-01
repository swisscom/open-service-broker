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

/*
 * Modified by Swisscom (Schweiz) AG) on 11th of June 2018
 */

package com.swisscom.cloud.sb.broker.services.credhub

import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.credhub.autoconfig.CredHubAutoConfiguration.ClientFactoryWrapper
import org.springframework.credhub.autoconfig.CredHubTemplateAutoConfiguration
import org.springframework.credhub.autoconfig.security.CredHubCredentialsDetails
import org.springframework.credhub.configuration.OAuth2CredHubTemplateFactory
import org.springframework.credhub.core.CredHubOperations
import org.springframework.credhub.core.CredHubProperties
import org.springframework.credhub.core.CredHubTemplate
import org.springframework.security.oauth2.client.OAuth2RestTemplate
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails

/**
 * {@link EnableAutoConfiguration Auto-configuration} for {@link CredHubTemplate} with
 * OAuth2 credentials if 'spring.credhub.oauth2-resourceowner.username' property is provided.
 */
@Configuration
@AutoConfigureBefore(CredHubTemplateAutoConfiguration.class)
@ConditionalOnProperty(name = "spring.credhub.enable", havingValue = "true")
@ConditionalOnClass(name = "org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails")
class CredHubOAuth2TemplateAutoConfiguration {
    private final OAuth2CredHubTemplateFactory credHubTemplateFactory = new OAuth2CredHubTemplateFactory()

    /**
     * Bean that holds OAuth2 credential information for CredHub.
     *
     * @return the {@link ClientCredentialsResourceDetails} bean
     */
    @Bean
    @CredHubCredentialsDetails
    @ConfigurationProperties("spring.credhub.oauth2-resourceowner")
    ClientCredentialsResourceDetails credHubCredentialsDetails() {
        return new ClientCredentialsResourceDetails()
    }

    /**
     * Preconfigured {@link OAuth2RestTemplate} with OAuth2 credentials for CredHub.
     *
     * @param credHubProperties {@link CredHubProperties} for CredHub
     * @param credHubCredentialsDetails OAuth2 credentials for use with the {@link OAuth2RestTemplate}
     * @param clientFactoryWrapper a {@link ClientFactoryWrapper} to customize CredHub http requests
     * @return the {@link CredHubOperations} bean
     */
    @Bean
    @Primary
    CredHubOperations credHubTemplate(CredHubProperties credHubProperties,
                                      @CredHubCredentialsDetails ClientCredentialsResourceDetails credHubCredentialsDetails, ClientFactoryWrapper clientFactoryWrapper) {
        return credHubTemplateFactory.credHubTemplate(credHubCredentialsDetails,
                credHubProperties,
                clientFactoryWrapper.getClientHttpRequestFactory())
    }

    @Bean
    CredHubService credHubService() {
        return new CredHubServiceImpl()
    }

}
