package com.swisscom.cloud.sb.broker.cleanup;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.swagger.ApiException;
import com.ifountain.opsgenie.client.swagger.api.AlertApi;
import com.ifountain.opsgenie.client.swagger.model.CreateAlertRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;

public class OpsGenieAlertingClient implements AlertingClient {
    protected static final Logger LOGGER = LoggerFactory.getLogger(OpsGenieAlertingClient.class);
    private final OpsGenieAlertingClientConfiguration configuration;
    private final AlertApi apiClient;

    public OpsGenieAlertingClient(OpsGenieAlertingClientConfiguration configuration) {
        this.configuration = configuration;

        apiClient = new OpsGenieClient().alertV2();
        apiClient.getApiClient().setApiKey(configuration.getApiKey());
    }

    @Override
    public void alert(Failure failure) {
        LOGGER.error(failure.message(), failure.exception());

        // https://docs.opsgenie.com/docs/opsgenie-java-api#create-alert
        CreateAlertRequest createAlertRequest = new CreateAlertRequest();
        createAlertRequest.setMessage(failure.message());
        createAlertRequest.setDescription(format("%s <br /> %s", failure.description(), failure.exception().getMessage()));
        createAlertRequest.setPriority(configuration.getAlertPriority());

        try {
            apiClient.createAlert(createAlertRequest);
        } catch (ApiException alertException) {
            LOGGER.error("Failed to send OpsGenie alert", alertException);
        }
    }
}
