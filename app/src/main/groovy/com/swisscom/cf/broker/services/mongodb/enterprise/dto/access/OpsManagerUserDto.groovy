package com.swisscom.cf.broker.services.mongodb.enterprise.dto.access


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
        GROUP_OWNER,
        GROUP_READ_ONLY,
        GROUP_USER_ADMIN

        @Override
        String toString() {
            return name()
        }
    }
}
