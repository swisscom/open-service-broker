package com.swisscom.cloud.sb.broker.backup.shield.dto

/*
Example:
{
  "uuid":"a2d95b67-b829-482c-87ab-2a589d8e58bc",
  "key":"2016/07/11/2016-07-11-122400-2939eff0-c149-4259-9fe8-327942bd8495",
  "taken_at":"2016-07-11 12:24:01",
  "expires_at":"2016-07-21 12:24:01",
  "notes":"",
  "status":"valid",
  "purge_reason":"",
  "target_uuid":"8710b942-4ccd-437e-8f55-de1d984b25aa",
  "target_plugin":"mysql",
  "target_endpoint":"{\n  \"mysql_user\": \"root\",\n  \"mysql_password\": \"\",\n  \"mysql_host\": \"127.0.0.1\",\n  \"mysql_port\": \"3306\"\n}",
  "store_uuid":"bf98dfb3-7f0e-4c3e-a641-5cfafcae29ec",
  "store_plugin":"fs",
  "store_endpoint":"{\n  \"base_dir\": \"/var/vcap/store/backups\"\n}"}
 */

class ArchiveDto {
    String uuid
    String key
    String status
    Status statusParsed

    // possible statuses guessed by looking at https://github.com/starkandwayne/shield/blob/master/db/archives.go
    enum Status {
        VALID("valid"),
        INVALID("invalid"),
        PURGED("purged"),
        EXPIRED("expired")

        final String status

        Status(final String status) {
            this.status = status
        }

        String toString() {
            status
        }

        boolean isValid() {
            status == VALID.status
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
