package com.swisscom.cf.broker.services.common


class AbstractBindResponseDto implements BindResponseDto {
    String syslog_drain_url
    String route_service_url

    @Override
    String toJson() {
        return null
    }
}
