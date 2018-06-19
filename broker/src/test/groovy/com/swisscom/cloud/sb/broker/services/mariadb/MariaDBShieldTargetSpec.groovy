package com.swisscom.cloud.sb.broker.services.mariadb

import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Specification


class MariaDBShieldTargetSpec extends Specification {
    def "generates an endpoint json for mysql"() {
        given:
        def target = new MariaDBShieldTarget(
                user: 'user1',
                password: 'pw1',
                host: 'host1',
                database: 'db1')
        and:
        String expected = """{
                            "mysql_user": "user1",
                            "mysql_password": "pw1",
                            "mysql_host": "host1",
                            "mysql_database": "db1",
                            "mysql_options": "${MariaDBShieldTarget.MYSQL_OPTIONS}"
                        }"""
        expect:
        JSONAssert.assertEquals(expected, target.endpointJson(), false)
    }

}