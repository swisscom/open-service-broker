package com.swisscom.cloud.sb.broker.util

import spock.lang.Specification
import spock.lang.Unroll

class SensitiveParameterProviderSpec extends Specification {
    private SensitiveParameterProvider sut

    def setup() {
        sut = new SensitiveParameterProvider() {}
    }

    @Unroll
    def 'getSanitizedParameters(#parameters) should return confidential string: #expectedMap'() {
        when:
        Map<String, Object> result = sut.getSanitizedParameters(parameters)

        then:
        result == new HashMap<String, Object>(expectedMap)

        where:
        parameters                      | expectedMap
        null                            | [:]
        ["test": "hello", "foo": "bar"] | [CONFIDENTIAL: new HashSet(["test", "foo"])]
        ["test": ["foo": "bar"]]        | [CONFIDENTIAL: new HashSet(["test"])]
    }
}
