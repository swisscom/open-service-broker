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

import com.swisscom.cloud.sb.broker.cfextensions.extensions.ExtensionConfig
import groovy.transform.CompileStatic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@CompileStatic
@Configuration
@ConfigurationProperties(prefix = "com.swisscom.cloud.sb.broker.service.openwhisk")
class OpenWhiskConfig implements ExtensionConfig{
    String openWhiskUrl
    String openWhiskProtocol
    String openWhiskHost
    String openWhiskPath
    String openWhiskDbUser
    String openWhiskDbPass
    String openWhiskDbProtocol
    String openWhiskDbPort
    String openWhiskDbHost
    String openWhiskDbLocalUser
    String openWhiskDbHostname

    @Override
    public String toString() {
        return "OpenWhiskConfig{" +
                "openWhiskUrl= '" + openWhiskUrl + '\'' +
                ", openWhiskProtocol= '" + openWhiskProtocol + '\'' +
                ", openWhiskHost= '" + openWhiskHost + '\'' +
                ", openWhiskPath= '" + openWhiskPath + '\'' +
                ", openWhiskDbUser= '" + openWhiskDbUser + '\'' +
                ", openWhiskDbProtocol= '" + openWhiskDbProtocol + '\'' +
                ", openWhiskDbPort= '" + openWhiskDbPort + '\'' +
                ", openWhiskDbHost= '" + openWhiskDbHost + '\'' +
                ", openWhiskDbLocalUser= '" + openWhiskDbLocalUser + '\'' +
                ", openWhiskDbHostname= '" + openWhiskDbHostname + '\'' +
                "}"

    }
}
