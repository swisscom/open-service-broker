package com.swisscom.cloud.sb.broker.binding;

import com.swisscom.cloud.sb.broker.services.credhub.CredHubService;
import com.swisscom.cloud.sb.broker.services.credhub.OAuth2CredHubService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.credhub.support.ClientOptions;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class CredentialStoreConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "com.swisscom.cloud.sb.broker.credhub")
    public CredHubConfigurationProperties credHubConfigurationProperties() {
        return new CredHubConfigurationProperties();
    }

    @Bean
    public CredHubService oauth2CredHubService(CredHubConfigurationProperties credHubConfigurationProperties,
                                               ClientRegistrationRepository clientRegistrationRepository,
                                               OAuth2AuthorizedClientService authorizedClientService) {
        ClientOptions clientOptions = new ClientOptions();
        return OAuth2CredHubService.of(credHubConfigurationProperties.getUrl(),
                                       credHubConfigurationProperties.getRegistrationId(),
                                       clientOptions,
                                       clientRegistrationRepository,
                                       authorizedClientService);
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "osb.credential.store", havingValue = "credhub")
    public CredentialStore credHubCredentialStore(CredHubService credHubService) {
        return CredHubCredentialStore.of(credHubService);
    }
/*
    @Bean
    @ConditionalOnProperty(name = "osb.credential.store", havingValue = "default", matchIfMissing = true)
    public CredentialStore defaultCredentialStore() {
        return DefaultCredentialStore.of();
    }*/

    public static class CredHubConfigurationProperties {
        private URI url;
        private List<String> cacerts = new ArrayList();
        private Map<String, String> oauth2 = new HashMap();


        public URI getUrl() {
            return url;
        }

        public void setUrl(URI url) {
            this.url = url;
        }

        public Map<String, String> getOauth2() {
            return oauth2;
        }

        public void setOauth2(Map<String, String> oauth2) {
            this.oauth2 = oauth2;
        }

        public List<String> getCacerts() {
            return cacerts;
        }

        public void setCacerts(List<String> cacerts) {
            this.cacerts = cacerts;
        }

        public String getRegistrationId() {
            return oauth2.get("registration-id");
        }

        public String[] getUaaCaCert() {
            return cacerts.toArray(new String[0]);
        }
    }
}
