/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.broker.backup

import com.swisscom.cloud.sb.model.backup.RestoreStatus
import spock.lang.Specification

import static com.swisscom.cloud.sb.broker.model.Backup.Status.*

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
