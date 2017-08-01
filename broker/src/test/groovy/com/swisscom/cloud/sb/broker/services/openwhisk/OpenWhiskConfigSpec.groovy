package com.swisscom.cloud.sb.broker.services.openwhisk

import spock.lang.Specification

class OpenWhiskConfigSpec extends Specification {
    def "Verify config toString"() {
        given:
        OpenWhiskConfig openWhiskConfig = new OpenWhiskConfig(openWhiskUrl: "whiskUrl", openWhiskAdminKey: "adminKey",
                openWhiskProtocol: "whiskProtocol", openWhiskHost: "whiskHost",
                openWhiskPath: "whiskPath", openWhiskDbUser: "dbUser",
                openWhiskDbProtocol: "dbProtocol", openWhiskDbPort: "dbPort",
                openWhiskDbHost: "dbHost", openWhiskDbLocalUser: "dbLocalUser",
                openWhiskDbHostname: "dbHostname")
//    and:
//    String expected = """OpenWhiskConfig{openWhiskURL= 'whiskUrl'"""
    }
}
