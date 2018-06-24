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
enum BackupStatus implements Serializable {

    CREATE_IN_PROGRESS("CREATE_IN_PROGRESS"),
    CREATE_SUCCEEDED("CREATE_SUCCEEDED"),
    CREATE_FAILED("CREATE_FAILED"),
    DELETE_IN_PROGRESS("DELETE_IN_PROGRESS"),
    DELETE_SUCCEEDED("DELETE_SUCCEEDED"),
    DELETE_FAILED("DELETE_FAILED")

    final String status

    BackupStatus(String status) { this.status = status }

    static BackupStatus of(String status) {
        return BackupStatus.values().find { it.status == status }
    }

    @Override
    public String toString() {
        return status;
    }
}
