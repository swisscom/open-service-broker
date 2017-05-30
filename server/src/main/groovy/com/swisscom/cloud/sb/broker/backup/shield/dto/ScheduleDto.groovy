package com.swisscom.cloud.sb.broker.backup.shield.dto

/*
Example:
{
  "uuid":"26ab4120-079c-4e4f-98f7-80e8a281b8e9",
  "name":"schedu",
  "summary":"adfs",
  "when":"hourly at 24"}
 */

class ScheduleDto implements Serializable {
    String uuid
    String name
    String summary
    String when
}
