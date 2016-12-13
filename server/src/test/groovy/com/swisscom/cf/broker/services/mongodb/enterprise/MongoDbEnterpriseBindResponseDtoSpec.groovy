package com.swisscom.cf.broker.services.mongodb.enterprise

import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Specification

class MongoDbEnterpriseBindResponseDtoSpec extends Specification {

    def "json serialization works correctly"() {
        given:
        MongoDbEnterpriseBindResponseDto credentials = new MongoDbEnterpriseBindResponseDto(database: 'database',
                username: 'username',
                password: 'password',
                hosts: ['host'],
                port: 1234,
                replicaSet: 'replicaSet1',
                opsManagerUrl: 'url',
                opsManagerUser: 'opsUser',
                opsManagerPassword: 'opsPw')
        and:
        String expected = """{
                            "credentials": {
                                "host": "host",
                                "port": "1234",
                                "database": "database",
                                "username": "username",
                                "password": "password",
                                "database_uri": "mongodb://username:password@host:1234/database?replicaSet=replicaSet1",
                                "uri": "mongodb://username:password@host:1234/database?replicaSet=replicaSet1",
                                "ops_manager_url": "url",
                                "ops_manager_user": "opsUser",
                                "ops_manager_password": "opsPw",
                                "replica_set": "replicaSet1",
                            }
                        }"""
        expect:
        JSONAssert.assertEquals(expected, credentials.toJson(), true)
    }
}
