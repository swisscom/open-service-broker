package com.swisscom.cf.servicebroker.client.model

import com.fasterxml.jackson.annotation.JsonAutoDetect

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
class LastOperationResponse {
    String description
    LastOperationState state
}
