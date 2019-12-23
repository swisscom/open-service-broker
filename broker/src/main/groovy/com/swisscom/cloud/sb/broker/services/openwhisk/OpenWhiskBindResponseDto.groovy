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

package com.swisscom.cloud.sb.broker.services.openwhisk

import com.swisscom.cloud.sb.broker.services.credential.BindResponseDto
import groovy.json.JsonBuilder

class OpenWhiskBindResponseDto implements BindResponseDto {
    String openwhiskExecutionUrl
    String openwhiskAdminUrl
    String openwhiskUUID
    String openwhiskKey
    String openwhiskNamespace
    String openwhiskSubject

    @Override
    String toJson() {
        def jsonBuilder = createBuilder()
        return jsonBuilder.toPrettyString()
    }

    protected JsonBuilder createBuilder() {
        def jsonBuilder = new JsonBuilder()
        jsonBuilder.credentials(
                executionUrl: openwhiskExecutionUrl,
                adminUrl: openwhiskAdminUrl,
                uuid: openwhiskUUID,
                key: openwhiskKey,
                namespace: openwhiskNamespace,
                subject: openwhiskSubject
        )
        return jsonBuilder
    }
}
