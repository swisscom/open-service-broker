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

import com.swisscom.cloud.sb.broker.model.Backup
import com.swisscom.cloud.sb.model.backup.RestoreStatus
import groovy.transform.CompileStatic

@CompileStatic
class RestoreStatusConverter {
    RestoreStatus convert(Backup.Status status) {
        switch (status) {
            case Backup.Status.INIT:
            case Backup.Status.IN_PROGRESS:
                return RestoreStatus.IN_PROGRESS
            case Backup.Status.SUCCESS:
                return RestoreStatus.SUCCEEDED
            case Backup.Status.FAILED:
                return RestoreStatus.FAILED
            default:
                throw new RuntimeException("Unknown status:${status}")
        }
    }
}
