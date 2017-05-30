package com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.automation


class LogRotateDto implements Serializable {
    int sizeThresholdMB
    int timeThresholdHrs
}
