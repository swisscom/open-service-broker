package com.swisscom.cf.broker.services.mongodb.enterprise.dto.automation

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
