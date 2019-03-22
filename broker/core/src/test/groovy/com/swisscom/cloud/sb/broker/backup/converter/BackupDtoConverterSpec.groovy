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

import com.swisscom.cloud.sb.broker.backup.BackupStatusConverter
import com.swisscom.cloud.sb.broker.model.Backup
import com.swisscom.cloud.sb.model.backup.BackupStatus
import spock.lang.Specification

class BackupDtoConverterSpec extends Specification {
    private BackupDtoConverter backupDtoConverter
    private RestoreDtoConverter restoreDtoConverter

    def setup() {
        restoreDtoConverter = Mock(RestoreDtoConverter)
        backupDtoConverter = new BackupDtoConverter(new BackupStatusConverter(), restoreDtoConverter)
    }

    def "conversion happy path"() {
        given:
        def source = new Backup()
        source.guid = "someId"
        source.serviceInstanceGuid = "serviceInstanceId"
        source.status = Backup.Status.SUCCESS
        source.operation = Backup.Operation.CREATE

        when:
        def result = backupDtoConverter.convert(source)
        then:
        source.guid == result.id
        source.serviceInstanceGuid == result.service_instance_id
        source.dateRequested == result.created_at
        source.dateUpdated == result.updated_at
        BackupStatus.CREATE_SUCCEEDED == result.status
    }
}
