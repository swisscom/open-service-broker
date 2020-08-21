package com.swisscom.cloud.sb.broker.cleanup;

import com.ifountain.opsgenie.client.swagger.model.CreateAlertRequest;

public class OpsGenieAlertingClientConfiguration {
    private String apiKey;
    private CreateAlertRequest.PriorityEnum alertPriority = CreateAlertRequest.PriorityEnum.P3;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public CreateAlertRequest.PriorityEnum getAlertPriority() {
        return alertPriority;
    }

    public void setAlertPriority(CreateAlertRequest.PriorityEnum alertPriority) {
        this.alertPriority = alertPriority;
    }
}
