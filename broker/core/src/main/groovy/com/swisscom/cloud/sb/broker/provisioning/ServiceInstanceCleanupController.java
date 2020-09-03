package com.swisscom.cloud.sb.broker.provisioning;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.NotSupportedException;

@RestController
public class ServiceInstanceCleanupController {
    private final ServiceInstanceCleanup serviceInstanceCleanup;

    ServiceInstanceCleanupController(ServiceInstanceCleanup serviceInstanceCleanup) {
        this.serviceInstanceCleanup = serviceInstanceCleanup;
    }

    @RequestMapping(value = "admin/service_instances/cleanup", method = RequestMethod.POST)
    void cleanupServiceInstances() throws NotSupportedException {
        new Thread(() -> serviceInstanceCleanup.cleanOrphanedServiceInstances()).start();
    }
}
