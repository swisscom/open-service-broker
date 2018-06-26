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

import groovy.transform.CompileStatic

@CompileStatic
class AuthenticationDto implements Serializable {
    boolean disabled

    String autoUser
    String autoPwd
    String autoAuthMechanism
    String keyfile
    String keyfileWindows
    String key
    List<DbUser> usersWanted
    List<DbUser2Delete> usersDeleted

    public static class DbUser implements Serializable {
        String db
        String user
        String initPwd
        List<DbRole> roles
    }

    public static class DbUser2Delete implements Serializable {
        String user
        List<String> dbs
    }

    public static class DbRole implements Serializable {
        String db
        String role

        static DbRole of(String db, String role) { return new DbRole(db: db, role: role) }
    }
}
