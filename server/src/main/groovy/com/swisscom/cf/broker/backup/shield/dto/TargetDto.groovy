package com.swisscom.cf.broker.backup.shield.dto

/*
 Example:
 {
   "uuid":"8710b942-4ccd-437e-8f55-de1d984b25aa",
   "name":"mysql",
   "summary":"mysql",
   "plugin":"mysql",
   "endpoint":"{\n  \"mysql_user\": \"root\",\n  \"mysql_password\": \"\",\n  \"mysql_host\": \"127.0.0.1\",\n  \"mysql_port\": \"3306\"\n}",
   "agent":"10.244.2.2:5444"
 }
 */

class TargetDto implements Serializable {
    String uuid
    String name
    String summary
    String plugin
    String agent
}
