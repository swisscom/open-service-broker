package com.swisscom.cloud.sb.broker.backup.shield.dto

/*
Example:
{
  "uuid":"bf98dfb3-7f0e-4c3e-a641-5cfafcae29ec",
  "name":"local",
  "summary":"asdf",
  "plugin":"fs",
  "endpoint":"{\n  \"base_dir\": \"/var/vcap/store/backups\"\n}"
 }
 */

class StoreDto implements Serializable {
    String uuid
    String name
    String summary
    String plugin
}
