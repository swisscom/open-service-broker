package com.swisscom.cf.broker.provisioning.lastoperation

import com.swisscom.cf.broker.async.lastoperation.CFLastOperationStatus
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
