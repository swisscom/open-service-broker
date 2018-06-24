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

/*
Example:
{
  "uuid":"4c201cad-6168-42e6-908e-e7dba663805c",
  "name":"mysqls3",
  "summary":"",
  "retention_name":"default",
  "retention_uuid":"8cec7ce4-9555-4dd8-b20d-990fbabc1456",
  "expiry":864000,
  "schedule_name":"schedu",
  "schedule_uuid":"26ab4120-079c-4e4f-98f7-80e8a281b8e9",
  "schedule_when":"hourly at 24",
  "paused":false,
  "store_uuid":"bf98dfb3-7f0e-4c3e-a641-5cfafcae29ec",
  "store_name":"local",
  "store_plugin":"fs",
  "store_endpoint":"{\n  \"base_dir\": \"/var/vcap/store/backups\"\n}",
  "target_uuid":"8710b942-4ccd-437e-8f55-de1d984b25aa",
  "target_name":"mysql",
  "target_plugin":"mysql",
  "target_endpoint":"{\n  \"mysql_user\": \"root\",\n  \"mysql_password\": \"\",\n  \"mysql_host\": \"127.0.0.1\",\n  \"mysql_port\": \"3306\"\n}",
  "agent":"10.244.2.2:5444"
}

 */

class JobDto implements Serializable {
    String uuid
    String name
    String summary
    String retention_uuid
    String schedule_uuid
    String store_uuid
    String target_uuid
}
