package com.swisscom.cloud.sb.broker.cleanup


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@EnableScheduling
@ConditionalOnBean(value = CleanupService)
class ScheduleConfiguration {
    @Autowired()
    CleanupService cleanupService;

    @Scheduled(cron = "0 15 2 * * *")
    void triggerCleanup() {
        cleanupService.triggerCleanup();
    }
}
