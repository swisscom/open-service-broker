package com.swisscom.cf.broker.services.mongodb.enterprise.dto.automation


class WhiteListDto implements Serializable {
    String ipAddress
    transient int count
    transient Date created
}
