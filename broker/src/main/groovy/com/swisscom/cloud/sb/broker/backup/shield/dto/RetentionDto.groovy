package com.swisscom.cloud.sb.broker.backup.shield.dto

/*
Example:
{
  "uuid":"8cec7ce4-9555-4dd8-b20d-990fbabc1456",
  "name":"default",
  "summary":"",
  "expires":864000
}
 */

class RetentionDto implements Serializable {
    String uuid
    String name
    String summary
    int expires
}
