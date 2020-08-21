package com.swisscom.cloud.sb.broker.cleanup;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CleanupController {
    private final CleanupService cleanupService;

    CleanupController(CleanupService cleanupService) {
        this.cleanupService = cleanupService;
    }

    @RequestMapping(value = "admin/cleanup/trigger", method = RequestMethod.POST)
    void manualTriggerCleanup() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                cleanupService.triggerCleanup();
            }
        }).start();
    }
}
