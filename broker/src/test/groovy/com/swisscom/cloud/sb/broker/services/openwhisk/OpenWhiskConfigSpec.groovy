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

import spock.lang.Specification

class OpenWhiskConfigSpec extends Specification {
    def "Verify config toString"() {
        given:
        OpenWhiskConfig openWhiskConfig = new OpenWhiskConfig(openWhiskUrl: "whiskUrl",
                openWhiskProtocol: "whiskProtocol", openWhiskHost: "whiskHost",
                openWhiskPath: "whiskPath", openWhiskDbUser: "dbUser",
                openWhiskDbProtocol: "dbProtocol", openWhiskDbPort: "dbPort",
                openWhiskDbHost: "dbHost", openWhiskDbLocalUser: "dbLocalUser",
                openWhiskDbHostname: "dbHostname")
        and:
        String expected = "OpenWhiskConfig{" +
                "openWhiskUrl= 'whiskUrl'" +
                ", openWhiskProtocol= 'whiskProtocol'" +
                ", openWhiskHost= 'whiskHost'" +
                ", openWhiskPath= 'whiskPath'" +
                ", openWhiskDbUser= 'dbUser'" +
                ", openWhiskDbProtocol= 'dbProtocol'" +
                ", openWhiskDbPort= 'dbPort'" +
                ", openWhiskDbHost= 'dbHost'" +
                ", openWhiskDbLocalUser= 'dbLocalUser'" +
                ", openWhiskDbHostname= 'dbHostname'" +
                "}"
        expect:
        openWhiskConfig.toString() == expected
    }
}
