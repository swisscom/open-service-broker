package com.swisscom.cloud.sb.broker.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

@Configuration
@EnableScheduling
public class CleanupConfiguration {
    protected static final Logger LOGGER = LoggerFactory.getLogger(CleanupConfiguration.class);

    @Scheduled(cron = "0 15 2 * * *")
    public void triggerCleanup() {
        LOGGER.info("## Time: {}", LocalDateTime.now());
    }
}
