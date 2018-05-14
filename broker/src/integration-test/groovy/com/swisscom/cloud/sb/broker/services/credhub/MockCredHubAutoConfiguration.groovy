package com.swisscom.cloud.sb.broker.services.credhub

import com.swisscom.cloud.sb.broker.config.ConditionalOnSystemProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnSystemProperty('mock_credhub')
class MockCredHubAutoConfiguration {

    static final String SYSTEM_PROPERTY_MOCK_CREDHUB = "mock_credhub"

    @Bean
    CredHubService credHubService() {
        return new MockCredHubService()
    }

}
