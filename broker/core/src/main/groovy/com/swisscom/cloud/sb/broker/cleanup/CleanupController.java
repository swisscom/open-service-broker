package com.swisscom.cloud.sb.broker.cleanup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.NotSupportedException;

@RestController
public class CleanupController {
    private final CleanupService cleanupService;

    CleanupController(@Autowired(required = false) CleanupService cleanupService) {
        this.cleanupService = cleanupService;
    }

    @RequestMapping(value = "admin/cleanup", method = RequestMethod.POST)
    void manualTriggerCleanup() throws NotSupportedException {
        if (cleanupService == null) {
            throw new NotSupportedException("cleanup is not supported for this OSB");
        }

        new Thread(() -> cleanupService.triggerCleanup()).start();
    }

    @RequestMapping(value = "admin/service_instances/{serviceInstanceUuid}/cleanup", method = RequestMethod.POST)
    void manualTriggerCleanup(@PathVariable("serviceInstanceUuid") String serviceInstanceUuid) throws NotSupportedException {
        if (cleanupService == null) {
            throw new NotSupportedException("cleanup is not supported for this OSB");
        }

        new Thread(() -> cleanupService.triggerCleanup(serviceInstanceUuid)).start();
    }
}
