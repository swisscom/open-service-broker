package com.swisscom.cloud.sb.broker.cleanup;

import com.swisscom.cloud.sb.broker.services.inventory.InventoryService;
import org.springframework.data.util.Pair;

public class CleanupInfoService {
    public static final String COMPLETED_STATE = "cleanup_completed";
    public static final String PENDING_STATE = "cleanup_pending";
    public static final String STATE_FIELD_NAME = "cleanup_state";

    private final InventoryService inventoryService;

    public CleanupInfoService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public String getState(String serviceInstanceUuid) {
        return inventoryService
                .getAll(serviceInstanceUuid, STATE_FIELD_NAME)
                .stream()
                .findFirst()
                .orElse(Pair.of(STATE_FIELD_NAME, PENDING_STATE))
                .getSecond();
    }

    public boolean isCompletedState(String serviceInstanceUuid) {
        return getState(serviceInstanceUuid).equals(COMPLETED_STATE);
    }

    public String setState(String serviceInstanceUuid, String value) {
        inventoryService.set(serviceInstanceUuid, Pair.of(STATE_FIELD_NAME, value));
        return getState(serviceInstanceUuid);
    }

    public String setCompletedState(String serviceInstanceUuid) {
        return setState(serviceInstanceUuid, COMPLETED_STATE);
    }
}
