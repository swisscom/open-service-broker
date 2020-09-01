package com.swisscom.cloud.sb.broker.cleanup;

import com.google.common.base.Stopwatch;
import com.swisscom.cloud.sb.broker.model.ServiceInstance;
import com.swisscom.cloud.sb.broker.repository.ServiceInstanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class CleanupService {
    protected static final Logger LOGGER = LoggerFactory.getLogger(CleanupService.class);

    private final CleanupServiceConfiguration cleanupServiceConfiguration;
    private final CleanupInfoService cleanupInfoService;
    private final ServiceInstanceRepository serviceInstanceRepository;
    private final AlertingClient alertingClient;
    private final CleanupAction cleanupAction;

    CleanupService(CleanupServiceConfiguration cleanupServiceConfiguration,
                   CleanupInfoService cleanupInfoService,
                   ServiceInstanceRepository serviceInstanceRepository,
                   AlertingClient alertingClient,
                   CleanupAction cleanupAction) {
        this.cleanupServiceConfiguration = cleanupServiceConfiguration;
        this.cleanupInfoService = cleanupInfoService;
        this.serviceInstanceRepository = serviceInstanceRepository;
        this.alertingClient = alertingClient;
        this.cleanupAction = cleanupAction;
    }

    public void triggerCleanup() {
        LOGGER.debug("triggerCleanup()");

        Mono.when(listCleanupServiceInstances().stream()
                .map(this::triggerServiceInstanceCleanup)
                .collect(Collectors.toList()))
                .block();

        LOGGER.debug("triggerCleanup() >> done");
    }

    public void triggerCleanup(String serviceInstanceUuid) {
        triggerServiceInstanceCleanup(serviceInstanceRepository.findByGuid(serviceInstanceUuid)).block();
    }

    private Mono<Boolean> triggerServiceInstanceCleanup(ServiceInstance serviceInstance) {
        return Mono.just(serviceInstance.getGuid())
                .publishOn(Schedulers.boundedElastic())
                .flatMap(cleanupAction::executeCleanupServiceInstance)
                .onErrorResume(ex -> {
                    handleException(serviceInstance, ex);
                    return Mono.just(false);
                })
                .doOnNext(r -> {
                    if (r) {
                        cleanupInfoService.setCompletedState(serviceInstance.getGuid());
                    }
                });
    }

    private void handleException(ServiceInstance serviceInstance, Throwable ex) {
        alertingClient.alert(Failure.builder()
                .message(format("Cleanup ServiceInstance failed for %s", serviceInstance.getGuid()))
                .description(format("Error during cleanup: %s", ex.getMessage()))
                .exception(ex)
                .build());
    }

    private List<ServiceInstance> listCleanupServiceInstances() {
        Stopwatch watch = Stopwatch.createStarted();
        List<ServiceInstance> result = serviceInstanceRepository.findAll()
                .stream()
                .filter(ServiceInstance::isDeleted)
                .filter(this::isDeletedDefined)
                .filter(this::isAfterCleanupOffset)
                .filter(this::isNotCleanupCompleted)
                .collect(Collectors.toList());

        LOGGER.info("Found {} service instances for cleanup, took: {}ms", result.size(), watch.elapsed(TimeUnit.MILLISECONDS));
        return result;
    }

    private boolean isNotCleanupCompleted(ServiceInstance serviceInstance) {
        return !cleanupInfoService.isCompletedState(serviceInstance.getGuid());
    }

    private boolean isDeletedDefined(ServiceInstance serviceInstance) {
        return serviceInstance.getDateDeleted() != null;
    }

    private boolean isAfterCleanupOffset(ServiceInstance serviceInstance) {
        LOGGER.info("isAfterCleanupOffset({})", serviceInstance);
        return serviceInstance.getDateDeleted().toInstant()
                .plus(cleanupServiceConfiguration.getCleanupThresholdInDays(), ChronoUnit.DAYS)
                .isBefore(Instant.now());
    }
}
