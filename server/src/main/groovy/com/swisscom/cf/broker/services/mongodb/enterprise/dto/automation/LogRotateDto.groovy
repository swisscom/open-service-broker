package com.swisscom.cf.broker.services.mongodb.enterprise.dto.automation


class LogRotateDto implements Serializable {
    int sizeThresholdMB
    int timeThresholdHrs
}
