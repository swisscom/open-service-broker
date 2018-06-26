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

package com.swisscom.cloud.sb.broker.backup.shield

import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler

class ShieldRestResponseErrorHandler implements ResponseErrorHandler {

    @Override
    void handleError(ClientHttpResponse response) throws IOException{
        String errorMessage = "Rest call to Shield failed with status:${response.statusCode}"
        // Shield raises a 501 and not a 404 if you query GET /v1/task/null or /v1/task/; let's handle this as it were a 404
        if (HttpStatus.NOT_IMPLEMENTED == response.statusCode) {
            throw new ShieldResourceNotFoundException(errorMessage)
        } else {
            throw new ServiceBrokerException(errorMessage)
        }
    }

    @Override
    boolean hasError(ClientHttpResponse response) throws IOException {
        return !response.statusCode.'2xxSuccessful'
    }
}
