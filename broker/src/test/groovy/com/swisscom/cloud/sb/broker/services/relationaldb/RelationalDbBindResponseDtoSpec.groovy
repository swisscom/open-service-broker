package com.swisscom.cloud.sb.broker.services.relationaldb

import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Specification

class RelationalDbBindResponseDtoSpec extends Specification {

    def 'json serialization should work correctly'() {
        given:
        RelationalDbBindResponseDto credentials = new RelationalDbBindResponseDto(database: 'database',
                username: 'username',
                password: 'password',
                host: 'host',
                port: 1234,
                vendor: 'vendor')
        and:
        String expected = """{
                            "credentials": {
                                "host": "host",
                                "hostname": "host",
                                "port": 1234,
                                "name": "database",
                                "database": "database",
                                "username": "username",
                                "password": "password",
                                "database_uri": "vendor://username:password@host:1234/database?reconnect=true",
                                "uri": "vendor://username:password@host:1234/database?reconnect=true",
                                "jdbcUrl":"jdbc:vendor://host:1234/database?user=username&password=password"
                            }
                        }"""
        expect:
        JSONAssert.assertEquals(expected, credentials.toJson(), false)
    }
}
