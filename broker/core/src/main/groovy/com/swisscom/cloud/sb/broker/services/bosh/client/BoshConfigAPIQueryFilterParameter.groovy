package com.swisscom.cloud.sb.broker.services.bosh.client

enum BoshConfigAPIQueryFilterParameter {
    NAME('name'), TYPE('type'), LATEST('latest')

    private String value

    BoshConfigAPIQueryFilterParameter(final String value) {
        this.value = value
    }

    public String getValue() {
        return value
    }

    @Override
    public String toString() {
        return this.getValue()
    }
}