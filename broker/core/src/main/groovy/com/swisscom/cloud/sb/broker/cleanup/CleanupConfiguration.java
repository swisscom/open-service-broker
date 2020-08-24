package com.swisscom.cloud.sb.broker.cleanup;

import com.swisscom.cloud.sb.broker.repository.ServiceInstanceRepository;
import com.swisscom.cloud.sb.broker.services.inventory.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("com.swisscom.cloud.sb.broker.cleanup")
public class CleanupConfiguration {
    protected static final Logger LOGGER = LoggerFactory.getLogger(CleanupConfiguration.class);

    private OpsGenieAlertingClientConfiguration opsGenie;
    private CleanupServiceConfiguration config;

    @Bean
    @ConditionalOnProperty(value = "com.swisscom.cloud.sb.broker.cleanup.opsgenie.apiKey")
    public AlertingClient opsGenieAlertingClient() {
        return new OpsGenieAlertingClient(getOpsGenie());
    }

    @Bean
    @ConditionalOnBean(value = CleanupAction.class)
    public CleanupService cleanupService(InventoryService inventoryService,
                                         ServiceInstanceRepository instanceRepository,
                                         AlertingClient alertingClient,
                                         CleanupAction cleanupAction) {
        return new CleanupService(getConfig(),
                new CleanupInfoService(inventoryService),
                instanceRepository,
                alertingClient,
                cleanupAction);
    }

    public OpsGenieAlertingClientConfiguration getOpsGenie() {
        return opsGenie;
    }

    public void setOpsGenie(OpsGenieAlertingClientConfiguration opsGenie) {
        this.opsGenie = opsGenie;
    }

    public CleanupServiceConfiguration getConfig() {
        return config;
    }

    public void setConfig(CleanupServiceConfiguration cleanup) {
        this.config = cleanup;
    }
}
