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

package com.swisscom.cloud.sb.broker.error

import groovy.transform.CompileStatic
import org.springframework.http.HttpStatus

@CompileStatic
class ServiceBrokerException extends RuntimeException {
    String description
    String code
    String error_code
    HttpStatus httpStatus

    ServiceBrokerException(String description, String code, String error_code, HttpStatus httpStatus) {
        super(description)
        this.description = description
        this.code = code
        this.error_code = error_code
        this.httpStatus = httpStatus
    }

    ServiceBrokerException(String description) {
        this(description, null, null, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
