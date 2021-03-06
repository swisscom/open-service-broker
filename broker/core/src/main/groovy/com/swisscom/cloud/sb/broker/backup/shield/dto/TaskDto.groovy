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

package com.swisscom.cloud.sb.broker.backup.shield.dto

import com.fasterxml.jackson.annotation.JsonValue

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
    UUID uuid
    String owner
    Type type
    UUID job_uuid
    UUID archive_uuid
    Status status
    String started_at
    String stopped_at
    String log

    @Override
    public String toString() {
        return "TaskDto{" +
                "uuid='" + uuid.toString() + '\'' +
                ", owner='" + owner + '\'' +
                ", type='" + type + '\'' +
                ", job_uuid='" + job_uuid.toString() + '\'' +
                ", archive_uuid='" + archive_uuid.toString() + '\'' +
                ", status='" + status + '\'' +
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

        private final String type

        Type(final String type) {
            this.type = type
        }

        String toString() {
            type
        }

        @JsonValue
        public String type() {
            return this.type
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

        private final String status

        Status(final String status) {
            this.status = status
        }

        String toString() {
            status
        }

        @JsonValue
        public String status() {
            return this.status
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
