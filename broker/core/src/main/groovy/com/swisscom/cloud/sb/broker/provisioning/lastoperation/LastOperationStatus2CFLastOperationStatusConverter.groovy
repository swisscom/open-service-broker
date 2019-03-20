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

package com.swisscom.cloud.sb.broker.provisioning.lastoperation

import com.swisscom.cloud.sb.broker.model.LastOperation
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

@CompileStatic
@Component
class LastOperationStatus2CFLastOperationStatusConverter {
    CFLastOperationStatus convert(LastOperation.Status jobStatus) {
        switch (jobStatus) {
            case LastOperation.Status.SUCCESS:
                return CFLastOperationStatus.SUCCEEDED
            case LastOperation.Status.FAILED:
                return CFLastOperationStatus.FAILED
            case LastOperation.Status.IN_PROGRESS:
                return CFLastOperationStatus.IN_PROGRESS
            default:
                throw new RuntimeException("Unknown CFLastOperationStatus:${jobStatus.toString()}")
        }
    }
}
