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

package com.swisscom.cloud.sb.broker.provisioning.lastoperation

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonValue
import groovy.transform.CompileStatic

@CompileStatic
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum CFLastOperationStatus {
    IN_PROGRESS('in progress'),
    SUCCEEDED('succeeded'),
    FAILED('failed')

    final String status

    CFLastOperationStatus(String status) { this.status = status }

    static CFLastOperationStatus of(String status) {
        return CFLastOperationStatus.values().find { it.status == status }
    }

    @Override
    @JsonValue
    public String toString() {
        return status;
    }
}