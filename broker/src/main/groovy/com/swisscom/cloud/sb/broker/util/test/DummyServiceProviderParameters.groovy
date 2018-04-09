package com.swisscom.cloud.sb.broker.util.test

import com.fasterxml.jackson.annotation.JsonProperty

import javax.validation.constraints.NotNull

class DummyServiceProviderParameters {
    String mode
    Integer delay
    String success

    @NotNull
    @JsonProperty("parent_reference")
    String parentReference
}
