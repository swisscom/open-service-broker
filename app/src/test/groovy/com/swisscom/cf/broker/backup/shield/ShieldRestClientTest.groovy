package com.swisscom.cf.broker.backup.shield

import org.springframework.web.client.RestTemplate
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Stepwise

@Stepwise
@Ignore
class ShieldRestClientTest extends Specification {
    ShieldRestClient restClient

    void setup() {
        restClient = new ShieldRestClient(new RestTemplate(), "http://localhost:8001", "averyhardkey", "10.244.2.2:5444")
    }

    def "obtain status"() {
        when:
        def status = restClient.getStatus()
        then:
        status
    }

    def "get store by name"() {
        when:
        def store = restClient.getStoreByName("local")
        then:
        store
    }

    def "get store by name not found"() {
        when:
        def store = restClient.getStoreByName("notexisting")
        then:
        store == null
    }

    def "get retention by name"() {
        when:
        def retention = restClient.getRetentionByName("default")
        then:
        retention
    }

    def "get retention by name not found"() {
        when:
        def retention = restClient.getRetentionByName("notexisting")
        then:
        retention == null
    }

    def "get schedule by name"() {
        when:
        def schedule = restClient.getScheduleByName("schedu")
        then:
        schedule
    }

    def "get schedule by name not found"() {
        when:
        def schedule = restClient.getScheduleByName("notexisting")
        then:
        schedule == null
    }
}
