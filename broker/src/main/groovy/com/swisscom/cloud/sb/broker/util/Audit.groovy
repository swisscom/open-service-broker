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

package com.swisscom.cloud.sb.broker.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.swisscom.cloud.sb.broker.error.ErrorCode
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
class Audit {

    enum AuditAction {
        Provision,
        Update,
        Bind,
        Unbind,
        Deprovision,
        Delete,
        Scheduler
    }

    static log(String message) {
        log(message, null)
    }

    static log(String message, Map parameters) {
        if (parameters != null) {
            message += " (${stringifyParameters(parameters)}"
        }
        ensureLog(message)
    }

    static String stringifyParameters(Map parameters) {
        ArrayList<String> kvpList = new ArrayList<String>()
        def objectMapper = new ObjectMapper()

        parameters.each { k,v -> kvpList.add("$k:${objectMapper.writeValueAsString(v)}".toString())}

        String.join(",", kvpList)
    }

    private static void ensureLog(String message) {
        String formattedString = "[!Audit]${message}[/!Audit]"

        if (log.infoEnabled)
            log.info(formattedString)
        else if (log.warnEnabled)
            log.warn(formattedString)
        else
            ErrorCode.SERVICEPROVIDER_INTERNAL_ERROR.throwNew("AuditLogging not possible. Log Level must have Warn or Info")
    }
}
