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

package com.swisscom.cloud.sb.broker.services

import com.google.common.base.Preconditions
import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

import java.beans.Introspector

@Component
@Slf4j
class ServiceProviderLookup implements ServiceProviderService{
    public static final String POSTFIX_SERVICE_PROVIDER = "ServiceProvider"

    @Autowired
    private ApplicationContext appContext

    @Override
    ServiceProvider findServiceProvider(String name) {

        log.info("Lookup for bean:${name}")

        return appContext.getBean(name)
    }

    @Override
    ServiceProvider findServiceProvider(Plan plan) {
        Preconditions.checkNotNull(plan, "A valid plan is needed for finding the corresponding ServiceProvider")

        if (plan?.serviceProviderClass) {
            log.info("Service provider lookup will be based on PLAN serviceProviderName:${plan.serviceProviderClass}")
            return findServiceProvider(plan.serviceProviderClass)
        }

        if (plan?.internalName) {
            log.info("Service provider lookup will be based on PLAN internalName:${plan.internalName}")
            return findServiceProvider(plan.internalName + POSTFIX_SERVICE_PROVIDER)
        }

        if (plan?.service?.serviceProviderClass){
            return findServiceProvider(plan.service.serviceProviderClass)
        }

        return findServiceProvider(plan.service.internalName + POSTFIX_SERVICE_PROVIDER)
    }

    @Override
    ServiceProvider findServiceProvider(CFService service, Plan plan){
        Preconditions.checkNotNull(service, "A valid service is needed for finding the corresponding ServiceProvider")

        if(service?.serviceProviderClass){
            return findServiceProvider(service.serviceProviderClass)
        }

        if(service?.internalName){
            return findServiceProvider(service.internalName + POSTFIX_SERVICE_PROVIDER)
        }

        return findServiceProvider(plan)
    }

    @Override
    String findInternalName(Class clazz) {
        def partialClassName = clazz.getSimpleName().substring(0, clazz.getSimpleName().lastIndexOf(POSTFIX_SERVICE_PROVIDER))
        return Introspector.decapitalize(partialClassName)
    }
}
