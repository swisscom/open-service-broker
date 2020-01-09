package com.swisscom.cloud.sb.broker.services.credhub

import com.swisscom.cloud.sb.broker.repository.ServiceBindingRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.credhub.support.ClientOptions
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository

@Configuration
class CredHubServiceProviderConfiguration {

    @Value("com.swisscom.cloud.sb.broker.credhub.url")
    URI brokerCredHubUri
    @Value("com.swisscom.cloud.sb.broker.credhub.oauth2.registration-id")
    String brokerOAuth2RegistrationId

    // Name must be `credHubServiceProvider` so that the name is correctly resolved.
    @Bean(name = "credHubServiceProvider")
    @ConditionalOnProperty("com.swisscom.cloud.sb.broker.credhub.url")
    @ConditionalOnMissingBean
    CredHubServiceProvider getBrokerDefault(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService,
            ServiceBindingRepository serviceBindingRepository) {
        return new CredHubServiceProvider(
                OAuth2CredHubService.of(
                        brokerCredHubUri,
                        brokerOAuth2RegistrationId,
                        new ClientOptions(),
                        clientRegistrationRepository,
                        authorizedClientService
                ),
                serviceBindingRepository
        )
    }
}
