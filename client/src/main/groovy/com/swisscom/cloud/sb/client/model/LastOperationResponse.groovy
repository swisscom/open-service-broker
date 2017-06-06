package com.swisscom.cloud.sb.client.model

import com.fasterxml.jackson.annotation.JsonAutoDetect

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
class LastOperationResponse {
    String description
    LastOperationState state
}
