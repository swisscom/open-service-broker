package com.swisscom.cloud.sb.broker.services

import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider

interface ServiceProviderService {

    ServiceProvider findServiceProvider(String name)

    ServiceProvider findServiceProvider(Plan plan)

    ServiceProvider findServiceProvider(CFService service, Plan plan)
}