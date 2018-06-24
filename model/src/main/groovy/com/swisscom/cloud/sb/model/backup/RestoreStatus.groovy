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

package com.swisscom.cloud.sb.model.backup

import groovy.transform.CompileStatic

@CompileStatic
enum RestoreStatus implements Serializable {

    IN_PROGRESS('IN_PROGRESS'),
    SUCCEEDED('SUCCEEDED'),
    FAILED('FAILED')

    final String status

    RestoreStatus(String status) { this.status = status }

    static RestoreStatus of(String status) {
        return RestoreStatus.values().find { it.status == status }
    }

    @Override
    public String toString() {
        return status;
    }
}