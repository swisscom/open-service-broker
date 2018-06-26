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

import com.swisscom.cloud.sb.model.backup.BackupStatus
import spock.lang.Specification

import static com.swisscom.cloud.sb.broker.model.Backup.Operation.CREATE
import static com.swisscom.cloud.sb.broker.model.Backup.Operation.DELETE
import static com.swisscom.cloud.sb.broker.model.Backup.Status.*

class BackupStatusConverterSpec extends Specification {
    BackupStatusConverter backupStatusConverter = new BackupStatusConverter()

    def "conversion happy paths"() {
        expect:
        result == backupStatusConverter.convert(dbStatus, operation)
        where:
        operation | dbStatus    | result
        CREATE    | INIT        | BackupStatus.CREATE_IN_PROGRESS
        CREATE    | IN_PROGRESS | BackupStatus.CREATE_IN_PROGRESS
        CREATE    | FAILED      | BackupStatus.CREATE_FAILED
        CREATE    | SUCCESS     | BackupStatus.CREATE_SUCCEEDED
        DELETE    | INIT        | BackupStatus.DELETE_IN_PROGRESS
        DELETE    | IN_PROGRESS | BackupStatus.DELETE_IN_PROGRESS
        DELETE    | FAILED      | BackupStatus.DELETE_FAILED
        DELETE    | SUCCESS     | BackupStatus.DELETE_SUCCEEDED
    }
}
