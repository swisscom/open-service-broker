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

import com.swisscom.cloud.sb.broker.config.Config
import groovy.transform.CompileStatic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

import static com.google.common.base.Strings.isNullOrEmpty

@CompileStatic
@Component
@ConfigurationProperties(prefix = "com.swisscom.cloud.sb.broker.shield")
class ShieldConfig implements Config {
    String baseUrl
    String apiKey
    String jobPrefix
    String targetPrefix
    int maxRetryBackup
    String defaultTenantName
    String username
    String password
    String backOffDelay

    @Override
    String toString() {
        return String.format("ShieldConfig with URL '%s' with username '%s' and password '%s' and api key '%s'",
                             getBaseUrl(),
                             getUsername(),
                             isNullOrEmpty(getPassword()) ? " NO PASSWORD PROVIDED" : "<CONFIDENTIAL>",
                             isNullOrEmpty(getApiKey()) ? " NO API KEY PROVIDED" : "<CONFIDENTIAL>");
    }
}
