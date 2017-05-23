package com.swisscom.cf.broker.backup

import com.swisscom.cf.servicebroker.model.backup.RestoreStatus
import spock.lang.Specification

import static com.swisscom.cf.broker.model.Backup.Status.*

class RestoreStatusConverterSpec extends Specification {
    RestoreStatusConverter restoreStatusConverter = new RestoreStatusConverter()

    def "conversion happy paths"() {
        expect:
        result == restoreStatusConverter.convert(dbStatus)
        where:
        dbStatus    | result
        INIT        | RestoreStatus.IN_PROGRESS
        IN_PROGRESS | RestoreStatus.IN_PROGRESS
        FAILED      | RestoreStatus.FAILED
        SUCCESS     | RestoreStatus.SUCCEEDED
    }
}
