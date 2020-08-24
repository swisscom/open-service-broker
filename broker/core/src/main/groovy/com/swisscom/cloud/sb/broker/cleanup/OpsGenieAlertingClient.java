package com.swisscom.cloud.sb.broker.cleanup;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.swagger.ApiException;
import com.ifountain.opsgenie.client.swagger.api.AlertApi;
import com.ifountain.opsgenie.client.swagger.model.CreateAlertRequest;
import com.ifountain.opsgenie.client.swagger.model.TeamRecipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

import static java.lang.String.format;

public class OpsGenieAlertingClient implements AlertingClient {
    protected static final Logger LOGGER = LoggerFactory.getLogger(OpsGenieAlertingClient.class);
    private final OpsGenieAlertingClientConfiguration configuration;
    private final AlertApi apiClient;

    public OpsGenieAlertingClient(OpsGenieAlertingClientConfiguration configuration) {
        this.configuration = configuration;

        apiClient = new OpsGenieClient().alertV2();
        apiClient.getApiClient().setBasePath(configuration.getBaseUrl());
        apiClient.getApiClient().setApiKey(configuration.getApiKey());
    }

    @Override
    public void alert(Failure failure) {
        LOGGER.error(failure.message(), failure.exception());

        // https://docs.opsgenie.com/docs/alert-api
        // https://docs.opsgenie.com/docs/opsgenie-java-api#create-alert
        CreateAlertRequest createAlertRequest = new CreateAlertRequest()
                .message(configuration.getAlertMessage() + " " + failure.message())
                .description(format("<br /> %s <br /> exception: %s",
                        configuration.getAlertDescription(),
                        failure.description(),
                        failure.exception().getMessage()))
                .teams(configuration.getTeams()
                        .stream()
                        .map(t -> new TeamRecipient().name(t)).collect(Collectors.toList()))
                .tags(configuration.getTags())
                .priority(configuration.getAlertPriority());

        try {
            apiClient.createAlert(createAlertRequest);
        } catch (ApiException alertException) {
            LOGGER.error("Failed to send OpsGenie alert", alertException);
        }
    }
}
