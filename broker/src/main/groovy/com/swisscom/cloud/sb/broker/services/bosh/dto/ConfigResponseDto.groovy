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

package com.swisscom.cloud.sb.broker.services.bosh.dto

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

@CompileStatic
class ConfigResponseDto implements Serializable {
    String id
    String name
    String type
    String content
    String createdAt
    boolean deleted

    ConfigResponseDto(String json) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        jsonSlurper.parseText(json) as ConfigResponseDto
    }

    String toJson() {
        return JsonOutput.toJson(this)
    }
}
