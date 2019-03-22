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

package com.swisscom.cloud.sb.broker.backup.converter

import com.swisscom.cloud.sb.broker.backup.RestoreStatusConverter
import com.swisscom.cloud.sb.broker.model.Backup
import com.swisscom.cloud.sb.broker.model.Restore
import com.swisscom.cloud.sb.model.backup.RestoreStatus
import spock.lang.Specification

class RestoreDtoConverterSpec extends Specification {
    private RestoreDtoConverter restoreDtoConverter

    def setup() {
        restoreDtoConverter = new RestoreDtoConverter(new RestoreStatusConverter())
    }

    def "conversion happy path"() {
        given:
        def source = new Restore()
        source.guid = "someId"
        source.backup = new Backup(guid: "backupId")
        source.status = dbStatus
        when:
        def result = restoreDtoConverter.convert(source)
        then:
        source.guid == result.id
        source.backup.guid == result.backup_id
        source.dateRequested == result.created_at
        source.dateUpdated == result.updated_at
        dtoStatus == result.status
        where:
        dbStatus           | dtoStatus
        Backup.Status.INIT | RestoreStatus.IN_PROGRESS

    }
}
