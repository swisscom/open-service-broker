package com.swisscom.cloud.sb.broker.cleanup

import com.ifountain.opsgenie.client.swagger.model.CreateAlertRequest
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class OpsGenieAlertingClientSpec extends Specification {

    OpsGenieAlertingClient sut;

    void setup() {
        String apiKey = "<redacted>";

        sut = new OpsGenieAlertingClient(
                new OpsGenieAlertingClientConfiguration(
                        apiKey: apiKey,
                        alertPriority: CreateAlertRequest.PriorityEnum.P4,
                        tags: ["test", "osb"],
                        alertMessage: "this service specific message",
                        alertDescription: "<b>Miauz</b><br />well well well.<br />something else"
                )
        )
    }

    void 'alert(): should send failure alert'() {
        given:
        Failure failure = Failure.builder()
                .message("something bad happened")
                .description("really really bad")
                .exception(new ServiceBrokerException("baaaad"))
                .build();

        when:
        sut.alert(failure);

        then:
        noExceptionThrown();
    }
}
