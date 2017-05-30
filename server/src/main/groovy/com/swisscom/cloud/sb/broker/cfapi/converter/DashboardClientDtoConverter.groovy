package com.swisscom.cloud.sb.broker.cfapi.converter

import com.swisscom.cloud.sb.broker.cfapi.dto.DashboardClientDto
import com.swisscom.cloud.sb.broker.converter.AbstractGenericConverter
import com.swisscom.cloud.sb.broker.model.CFService
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

@CompileStatic
@Component
class DashboardClientDtoConverter extends AbstractGenericConverter<CFService, DashboardClientDto> {

    @Override
    DashboardClientDto convert(final CFService source) {
        if (!(source.dashboardClientId && source.dashboardClientSecret)) {
            return null
        }

        DashboardClientDto prototype = new DashboardClientDto()
        prototype.id = source.dashboardClientId
        prototype.secret = source.dashboardClientSecret
        prototype.redirect_uri = source.dashboardClientRedirectUri
        return prototype
    }

    @Override
    protected void convert(CFService source, DashboardClientDto prototype) {
    }
}
