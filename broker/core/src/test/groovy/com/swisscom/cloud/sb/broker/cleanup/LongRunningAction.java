package com.swisscom.cloud.sb.broker.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.UUID;

class LongRunningAction implements CleanupAction {
    protected static final Logger LOGGER = LoggerFactory.getLogger(LongRunningAction.class);

    private long sleepInterval = 500;
    private long sleepIterations = 10;

    private UUID id = UUID.randomUUID();

    @Override
    public Mono<Boolean> executeCleanupServiceInstance(String serviceInstanceUuid) {
        LOGGER.info("executeCleanupServiceInstance({})", serviceInstanceUuid);

        return Mono.fromCallable(() -> {
            for (int i = 0; i <= sleepIterations; i++) {
                LOGGER.info("{}: {} iterations of {} - {}", serviceInstanceUuid, i, sleepIterations, id);
                try {
                    Thread.sleep(sleepInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return true;
        });
    }
}
