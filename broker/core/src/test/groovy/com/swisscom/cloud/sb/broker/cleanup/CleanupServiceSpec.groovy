package com.swisscom.cloud.sb.broker.cleanup

import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.repository.ServiceInstanceRepository
import reactor.core.publisher.Mono
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Instant

class CleanupServiceSpec extends Specification {

    CleanupServiceConfiguration cleanupServiceConfiguration = new CleanupServiceConfiguration()
    CleanupInfoService cleanupInfoService = Mock(CleanupInfoService)
    ServiceInstanceRepository serviceInstanceRepository = Mock(ServiceInstanceRepository)
    AlertingClient alertingClient = Mock(AlertingClient)
    CleanupAction cleanupAction = Mock(CleanupAction)

    @Unroll
    void 'triggerCleanup(): should trigger action (#executedCalled) with service instance (#deleted,#dateDeleted,#isCompletedState)'() {
        given:
        CleanupService sut = new CleanupService(cleanupServiceConfiguration,
                cleanupInfoService,
                serviceInstanceRepository,
                alertingClient,
                cleanupAction)

        and:
        String serviceInstanceUuid = UUID.randomUUID().toString()
        1 * serviceInstanceRepository.findAll() >> [
                new ServiceInstance(
                        guid: serviceInstanceUuid,
                        deleted: deleted,
                        dateDeleted: Date.from(Instant.parse(dateDeleted))
                )]

        and:
        (isCompletedStateCalled ? 1 : 0) * cleanupInfoService.isCompletedState(serviceInstanceUuid) >> isCompletedState

        and:
        (executedCalled ? 1 : 0) * cleanupAction.executeCleanupServiceInstance(serviceInstanceUuid) >> Mono.just(true)

        when:
        sut.triggerCleanup();

        then:
        noExceptionThrown();

        and:
        (executedCalled ? 1 : 0) * cleanupInfoService.setCompletedState(serviceInstanceUuid) >> true


        where:
        deleted | dateDeleted               | isCompletedState | isCompletedStateCalled | executedCalled
        true    | "2019-01-01T00:00:00.00Z" | false            | true                   | true
        true    | "2019-01-01T00:00:00.00Z" | true             | true                   | false
        true    | Instant.now().toString()  | true             | false                  | false
        false   | "2019-01-01T00:00:00.00Z" | false            | false                  | false
    }

    @Ignore
    void 'triggerCleanup(): will do long runs in parallel and with fun'() {
        given:
        CleanupService sut = new CleanupService(cleanupServiceConfiguration,
                cleanupInfoService,
                serviceInstanceRepository,
                alertingClient,
                new LongRunningAction())

        and:
        String serviceInstanceUuid = UUID.randomUUID().toString()
        1 * serviceInstanceRepository.findAll() >> [
                new ServiceInstance(
                        guid: UUID.randomUUID().toString(),
                        deleted: true,
                        dateDeleted: Date.from(Instant.parse("2019-01-01T00:00:00.00Z"))
                ),
                new ServiceInstance(
                        guid: UUID.randomUUID().toString(),
                        deleted: true,
                        dateDeleted: Date.from(Instant.parse("2019-01-01T00:00:00.00Z"))
                ),
                new ServiceInstance(
                        guid: UUID.randomUUID().toString(),
                        deleted: true,
                        dateDeleted: Date.from(Instant.parse("2019-01-01T00:00:00.00Z"))
                ),
                new ServiceInstance(
                        guid: UUID.randomUUID().toString(),
                        deleted: true,
                        dateDeleted: Date.from(Instant.parse("2019-01-01T00:00:00.00Z"))
                ),
                new ServiceInstance(
                        guid: UUID.randomUUID().toString(),
                        deleted: true,
                        dateDeleted: Date.from(Instant.parse("2019-01-01T00:00:00.00Z"))
                )]

        and:
        cleanupInfoService.isCompletedState(serviceInstanceUuid) >> false

        when:
        sut.triggerCleanup();

        then:
        noExceptionThrown();
    }

    void 'triggerCleanup(): should alert if a cleanup failed'() {
        given:
        CleanupService sut = new CleanupService(cleanupServiceConfiguration,
                cleanupInfoService,
                serviceInstanceRepository,
                alertingClient,
                cleanupAction)

        and:
        String serviceInstanceUuid = UUID.randomUUID().toString()
        1 * serviceInstanceRepository.findAll() >> [
                new ServiceInstance(
                        guid: serviceInstanceUuid,
                        deleted: true,
                        dateDeleted: Date.from(Instant.parse("2019-01-01T00:00:00.00Z"))
                )]

        and:
        1 * cleanupInfoService.isCompletedState(serviceInstanceUuid) >> false

        and:
        1 * cleanupAction.executeCleanupServiceInstance(_) >> Mono.error(new RuntimeException("Craaaash"));

        when:
        sut.triggerCleanup();

        then:
        noExceptionThrown();

        and:
        1 * alertingClient.alert({
            Failure f ->
                f.message().equals("Cleanup ServiceInstance failed for ${serviceInstanceUuid}".toString()) &&
                        f.description().equals("Error during cleanup: Craaaash") &&
                        f.exception() instanceof RuntimeException;
        })

        and:
        0 * cleanupInfoService.setCompletedState(serviceInstanceUuid) >> true
    }
}
