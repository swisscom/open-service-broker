package com.swisscom.cloud.sb.model.health

class ServiceHealth implements Serializable {
    ServiceHealthStatus status

    @Override
    String toString() {
        return "ServiceHealth{status='" + status + "'}"
    }
}
