package com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.automation

import groovy.transform.CompileStatic

@CompileStatic
class AuthenticationDto implements Serializable {
    boolean disabled

    String autoUser
    String autoPwd
    String autoAuthMechanism
    String keyfile
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
