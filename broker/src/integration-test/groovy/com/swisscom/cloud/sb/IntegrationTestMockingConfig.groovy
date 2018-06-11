package com.swisscom.cloud.sb

import com.swisscom.cloud.sb.broker.services.credhub.CredHubService
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import spock.mock.DetachedMockFactory

@TestConfiguration
class IntegrationTestMockingConfig {

    def mockFactory = new DetachedMockFactory()

    @Bean
    CredHubService credHubServiceMock() {
        return mockFactory.Mock(CredHubService)
    }

}
