package com.swisscom.cloud.sb.broker.provisioning.lastoperation

import spock.lang.Specification

class CFLastOperationStatusSpec extends Specification {

    def 'enums are parsed correctly from strings'() {
        expect:
        res == CFLastOperationStatus.of(text)
        where:
        text                                     | res
        CFLastOperationStatus.IN_PROGRESS.status | CFLastOperationStatus.IN_PROGRESS
    }
}
