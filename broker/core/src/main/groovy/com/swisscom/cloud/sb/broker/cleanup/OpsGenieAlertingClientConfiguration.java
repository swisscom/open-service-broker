package com.swisscom.cloud.sb.broker.cleanup;

import com.ifountain.opsgenie.client.swagger.model.CreateAlertRequest;

import java.util.ArrayList;
import java.util.List;

public class OpsGenieAlertingClientConfiguration {
    private String baseUrl = "https://api.eu.opsgenie.com";
    private String apiKey;
    private List<String> tags = new ArrayList<>();
    private List<String> teams = new ArrayList<>();
    private String alertDescription;
    private String alertMessage;
    private CreateAlertRequest.PriorityEnum alertPriority = CreateAlertRequest.PriorityEnum.P4;

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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getTeams() {
        return teams;
    }

    public void setTeams(List<String> teams) {
        this.teams = teams;
    }

    public String getAlertDescription() {
        return alertDescription;
    }

    public void setAlertDescription(String alertDescription) {
        this.alertDescription = alertDescription;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
