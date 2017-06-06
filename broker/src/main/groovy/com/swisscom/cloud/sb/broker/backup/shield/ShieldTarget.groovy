package com.swisscom.cloud.sb.broker.backup.shield

interface ShieldTarget extends Serializable {
    String pluginName()

    String endpointJson()
}