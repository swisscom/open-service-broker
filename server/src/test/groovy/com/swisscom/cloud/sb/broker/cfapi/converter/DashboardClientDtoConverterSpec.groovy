package com.swisscom.cloud.sb.broker.cfapi.converter

import com.swisscom.cloud.sb.broker.model.CFService
import spock.lang.Specification


class DashboardClientDtoConverterSpec extends Specification {
    DashboardClientDtoConverter dashboardClientDtoConverter

    def setup() {
        dashboardClientDtoConverter = new DashboardClientDtoConverter()
    }

    def "dashboard clientId,clientSecret fields should be non-null for conversion"() {
        expect:
        null == dashboardClientDtoConverter.convert(source)
        where:
        source << [new CFService(dashboardClientSecret: null, dashboardClientId: 'id', dashboardClientRedirectUri: 'uri'),
                   new CFService(dashboardClientSecret: 'secret', dashboardClientId: null, dashboardClientRedirectUri: 'uri')]
    }

    def "conversion is done correctly"() {
        given:
        def secret = 'secret'
        def id = 'id'
        def uri = 'uri'
        when:
        def result = dashboardClientDtoConverter.convert(new CFService(dashboardClientSecret: secret, dashboardClientId: id, dashboardClientRedirectUri: uri))
        then:
        result.secret == secret
        result.id == id
        result.redirect_uri == uri
    }
}
