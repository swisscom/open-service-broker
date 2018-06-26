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

package com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.automation

class BackupConfigDto implements Serializable {
    String groupId
    String clusterId
    String statusName
    String storageEngineName
    String authMechanismName
    String username
    String password

    String syncSource
    List<String> excludedNamespaces

    @Override
    public String toString() {
        return "BackupConfigDto{" +
                "groupId='" + groupId + '\'' +
                ", clusterId='" + clusterId + '\'' +
                ", statusName='" + statusName + '\'' +
                ", storageEngineName='" + storageEngineName + '\'' +
                ", authMechanismName='" + authMechanismName + '\'' +
                ", username='" + username + '\'' +
                ", syncSource='" + syncSource + '\'' +
                ", excludedNamespaces=" + excludedNamespaces +
                '}';
    }

    static enum Status {
        STARTED, STOPPED, TERMINATING

        @Override
        String toString() {
            return name()
        }
    }


}
