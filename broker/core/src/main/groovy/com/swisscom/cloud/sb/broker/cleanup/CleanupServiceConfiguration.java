package com.swisscom.cloud.sb.broker.cleanup;

import com.swisscom.cloud.sb.broker.model.ServiceInstance;

public class CleanupServiceConfiguration {

    private int cleanupThresholdInDays = 30;
    private int maxParallelExecutions = 10;
    private int maxQueueSize = 1000;


    public int getCleanupThresholdInDays() {
        return cleanupThresholdInDays;
    }

    /**
     * Amount of days a {@link ServiceInstance} must have been deleted, before its cleaned up (Default: 30).
     */
    public void setCleanupThresholdInDays(int cleanupThresholdInDays) {
        this.cleanupThresholdInDays = cleanupThresholdInDays;
    }

    public int getMaxParallelExecutions() {
        return maxParallelExecutions;
    }

    /**
     * Maximum amount of parallel threads running a cleanup job (Default: 10).
     */
    public void setMaxParallelExecutions(int maxParallelExecutions) {
        this.maxParallelExecutions = maxParallelExecutions;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    /**
     * Maximum amount of `jobs` scheduled to be executed.
     */
    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }
}
