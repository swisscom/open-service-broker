package com.swisscom.cloud.sb.broker.cleanup;

import reactor.core.publisher.Mono;

public interface CleanupAction {
    Mono<Boolean> executeCleanupServiceInstance(String serviceInstanceUuid);
}
