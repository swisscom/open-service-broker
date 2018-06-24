/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.client.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

/*
* This class would not be needed if the model object with the link below could be deserialized correctly
 * https://github.com/spring-cloud/spring-cloud-cloudfoundry-service-broker/blob/master/src/main/java/org/springframework/cloud/servicebroker/model/OperationState.java
* */
enum LastOperationState {

    IN_PROGRESS("in progress"),
    SUCCEEDED("succeeded"),
    FAILED("failed")

    private final String state

    LastOperationState (String state) {
        this.state = state
    }

    @JsonCreator
    public static LastOperationState fromString( String key) {
        return key ? LastOperationState.values().find { it.value == key } :  null
    }

    @JsonValue
    public String getValue() {
        return state
    }

}

