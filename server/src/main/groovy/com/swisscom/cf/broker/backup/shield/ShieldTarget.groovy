package com.swisscom.cf.broker.backup.shield

interface ShieldTarget extends Serializable {
    String pluginName()

    String endpointJson()
}