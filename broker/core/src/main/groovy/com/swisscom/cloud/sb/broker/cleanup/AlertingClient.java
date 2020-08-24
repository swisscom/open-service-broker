package com.swisscom.cloud.sb.broker.cleanup;

public interface AlertingClient {

    /**
     * Sends an alert to an Alerting backend.
     */
    void alert(Failure failure);
}
