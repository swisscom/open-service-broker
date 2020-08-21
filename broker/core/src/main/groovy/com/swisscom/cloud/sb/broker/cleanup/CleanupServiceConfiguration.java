package com.swisscom.cloud.sb.broker.cleanup;

public class CleanupServiceConfiguration {

    private int cleanupThresholdInDays = 30;

    public int getCleanupThresholdInDays() {
        return cleanupThresholdInDays;
    }

    public void setCleanupThresholdInDays(int cleanupThresholdInDays) {
        this.cleanupThresholdInDays = cleanupThresholdInDays;
    }
}
