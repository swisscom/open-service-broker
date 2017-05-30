package com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.automation


class ProcessArgumentsV26Dto implements Serializable {
    Net net
    Storage storage
    SystemLog systemLog
    Replication replication

    static class Net implements Serializable {
        int port
    }

    static class Storage implements Serializable {
        String dbPath
    }

    static class SystemLog implements Serializable {
        String path
        String destination
    }

    static class Replication implements Serializable {
        String replSetName
    }
}
