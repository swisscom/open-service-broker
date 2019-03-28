/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

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
