package com.swisscom.cloud.sb.model.usage.extended

import com.swisscom.cloud.sb.model.usage.ServiceUsageType

class ServiceUsageItem implements Serializable {
    String property
    String value
    String unit

    ServiceUsageType type

    @Override
    public String toString() {
        return "ServiceUsageItem{" +
                "property=" + property +
                ", value='" + value + '\'' +
                ", unit=" + unit +
                ", type=" + type +
                '}';
    }
}
