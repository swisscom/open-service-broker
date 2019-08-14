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

package com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.access


class OpsManagerUserDto implements Serializable {
    String id
    String username
    String password
    String firstName
    String lastName
    List<Role> roles

    static class Role implements Serializable {
        String groupId
        String roleName
    }


    enum UserRole {
        GROUP_AUTOMATION_ADMIN,
        GROUP_BACKUP_ADMIN,
        GROUP_MONITORING_ADMIN,
        GROUP_DATA_ACCESS_ADMIN,
        GROUP_DATA_ACCESS_READ_ONLY,
        GROUP_DATA_ACCESS_READ_WRITE,
        GROUP_OWNER,
        GROUP_READ_ONLY,
        GROUP_USER_ADMIN

        @Override
        String toString() {
            return name()
        }
    }
}
