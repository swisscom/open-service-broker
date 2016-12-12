package com.swisscom.cf.broker.backup.shield.dto

/*
Example:
{
  "uuid":"000c5f16-c5b6-494c-9824-b68618645537",
  "owner":"system",
  "type":"backup",
  "job_uuid":"4c201cad-6168-42e6-908e-e7dba663805c",
  "archive_uuid":"a2d95b67-b829-482c-87ab-2a589d8e58bc",
  "status":"done",
  "started_at":"2016-07-11 12:24:00",
  "stopped_at":"2016-07-11 12:24:01",
  "log":"Validating environment...\n=========================\nSHIELD_OP ... found\nSHIELD_STORE_PLUGIN ... found\nSHIELD_STORE_ENDPOINT ... found\nSHIELD_TARGET_PLUGIN ... found\nSHIELD_TARGET_ENDPOINT ... found\nOK\n\nValidating TARGET plugin `mysql`...\n===================================\n✓ mysql_host          127.0.0.1\n✓ mysql_port          3306\n✓ mysql_user          root\n✓ mysql_password      \n✓ mysql_read_replica  no read replica\nOK\n\nValidating STORE plugin `fs`...\n===============================\n✓ base_dir  files in /var/vcap/store/backups will be backed up\n✓ include   all files will be included\n✓ exclude   no files will be excluded\n✓ bsdtar    using default /var/vcap/packages/bsdtar/bin/bsdtar\nOK\n\nRunning backup task (using bzip2 compression)\n=============================================\n\nEXITING 0\n"
}
 */

class TaskDto {
    String uuid
    String owner
    String type
    Type typeParsed
    String job_uuid
    String archive_uuid
    String status
    Status statusParsed
    String started_at
    String stopped_at
    String log

    @Override
    public String toString() {
        return "TaskDto{" +
                "uuid='" + uuid + '\'' +
                ", owner='" + owner + '\'' +
                ", type='" + type + '\'' +
                ", typeParsed=" + typeParsed +
                ", job_uuid='" + job_uuid + '\'' +
                ", archive_uuid='" + archive_uuid + '\'' +
                ", status='" + status + '\'' +
                ", statusParsed=" + statusParsed +
                ", started_at='" + started_at + '\'' +
                ", stopped_at='" + stopped_at + '\'' +
                ", log='" + log + '\'' +
                '}';
    }

    // types taken from https://github.com/starkandwayne/shield/blob/eda3ca15e1554097576260c21ebffd5ae6549caf/db/tasks.go#L15-17
    enum Type {
        BACKUP("backup"),
        RESTORE("restore"),
        PURGE("purge")

        final String type

        Type(final String type) {
            this.type = type
        }

        String toString() {
            type
        }

        boolean isBackup() {
            this == BACKUP
        }

        static Type of(String text) {
            def result = Type.values().find { it.type.toLowerCase() == text.toLowerCase() }
            if (!result) {
                throw new RuntimeException("Unknown type:${text}")
            }
            return result
        }
    }

    // statuses taken from https://github.com/starkandwayne/shield/blob/eda3ca15e1554097576260c21ebffd5ae6549caf/db/tasks.go#L19-23
    enum Status {
        PENDING("pending"),
        RUNNING("running"),
        CANCELED("canceled"),
        FAILED("failed"),
        DONE("done")

        final String status

        Status(final String status) {
            this.status = status
        }

        String toString() {
            status
        }

        boolean isRunning() {
            this == PENDING || this == RUNNING
        }

        boolean isFailed() {
            this == CANCELED || this == FAILED
        }

        boolean isDone() {
            this == DONE
        }

        static Status of(String text) {
            def result = Status.values().find { it.status.toLowerCase() == text.toLowerCase() }
            if (!result) {
                throw new RuntimeException("Unknown status:${text}")
            }
            return result
        }
    }
}
