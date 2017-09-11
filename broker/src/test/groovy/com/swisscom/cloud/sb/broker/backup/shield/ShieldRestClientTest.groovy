package com.swisscom.cloud.sb.broker.backup.shield

import com.swisscom.cloud.sb.broker.util.RestTemplateFactory
import spock.lang.IgnoreIf
import spock.lang.Specification
import spock.lang.Stepwise

@Stepwise
@IgnoreIf({ !Boolean.valueOf(System.properties['com.swisscom.cloud.sb.broker.run3rdPartyDependentTests']) })
class ShieldRestClientTest extends Specification {
    ShieldRestClient restClient

    void setup() {
        def restTemplateFactory = new RestTemplateFactory()
        restClient = new ShieldRestClient(restTemplateFactory.buildWithSSLValidationDisabled(), "https://localhost:18002", "averyhardkey")
    }

    def "obtain status"() {
        when:
        def status = restClient.getStatus()
        then:
        status
    }

    def "get store by name"() {
        when:
        def store = restClient.getStoreByName("default")
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
        def schedule = restClient.getScheduleByName("default")
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
