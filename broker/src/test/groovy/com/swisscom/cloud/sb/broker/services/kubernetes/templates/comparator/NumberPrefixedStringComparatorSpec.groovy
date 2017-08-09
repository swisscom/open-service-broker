package com.swisscom.cloud.sb.broker.services.kubernetes.templates.comparator

import spock.lang.Specification

class NumberPrefixedStringComparatorSpec extends Specification {


    NumberPrefixedStringComparator numberPrefixedStringComparatorSpec

    def setup() {
        numberPrefixedStringComparatorSpec = new NumberPrefixedStringComparator()

    }

    def "ascending order prefixed strings"() {
        when:
        int result = numberPrefixedStringComparatorSpec.compare("33aaav23sd","0zzzzz")
        then:
        result == 1
    }

    def "ascending order prefixed another strings"() {
        when:
        int result = numberPrefixedStringComparatorSpec.compare("033aaav23sd","0100zzzzz")
        then:
        result == -1
    }


}
