package com.swisscom.cloud.sb.broker.cleanup

import com.swisscom.cloud.sb.broker.services.inventory.InventoryService
import org.springframework.data.util.Pair
import spock.lang.Specification
import spock.lang.Unroll

class CleanupInfoServiceSpec extends Specification {

    InventoryService inventoryService = Mock(InventoryService);
    CleanupInfoService sut;

    void setup() {
        sut = new CleanupInfoService(inventoryService)
    }

    void 'getState(): should work with duplicated entries'() {
        given:
        String serviceInstanceUuid = UUID.randomUUID().toString();

        inventoryService.getAll(serviceInstanceUuid, CleanupInfoService.STATE_FIELD_NAME) >>
                [Pair.of(CleanupInfoService.STATE_FIELD_NAME, CleanupInfoService.COMPLETED_STATE),
                 Pair.of(CleanupInfoService.STATE_FIELD_NAME, CleanupInfoService.PENDING_STATE)]

        when:
        String state = sut.getState(serviceInstanceUuid);

        then:
        state == CleanupInfoService.COMPLETED_STATE;
    }

    @Unroll
    void 'getState(): should work with value:#result'() {
        given:
        String serviceInstanceUuid = UUID.randomUUID().toString();

        inventoryService.getAll(serviceInstanceUuid, CleanupInfoService.STATE_FIELD_NAME) >>
                [Pair.of(CleanupInfoService.STATE_FIELD_NAME, result)]

        when:
        String state = sut.getState(serviceInstanceUuid);

        then:
        state == result;

        where:
        result << [CleanupInfoService.COMPLETED_STATE, CleanupInfoService.PENDING_STATE, "Something"]
    }

    void 'getState(): should work no entries'() {
        given:
        String serviceInstanceUuid = UUID.randomUUID().toString();

        inventoryService.getAll(serviceInstanceUuid, CleanupInfoService.STATE_FIELD_NAME) >> []

        when:
        String state = sut.getState(serviceInstanceUuid);

        then:
        state == CleanupInfoService.PENDING_STATE;
    }
}
