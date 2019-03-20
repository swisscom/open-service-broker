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

package com.swisscom.cloud.sb.broker.provisioning

import com.swisscom.cloud.sb.broker.cfextensions.extensions.ExtensionProvider
import com.swisscom.cloud.sb.broker.context.CloudFoundryContextRestrictedOnly
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.util.ParentServiceProvider
import com.swisscom.cloud.sb.broker.util.SensitiveParameterProvider
import com.swisscom.cloud.sb.broker.util.servicecontext.ServiceContextHelper
import groovy.util.logging.Slf4j
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.CloudFoundryContext
import org.springframework.cloud.servicebroker.model.Context
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Slf4j
@Service
@Transactional
class ProvisioningService {
    @Autowired
    protected ServiceProviderLookup serviceProviderLookup
    @Autowired
    protected ProvisioningPersistenceService provisioningPersistenceService
    //TODO
    /*@Autowired
    BackupService backupService*/

    ProvisionResponse provision(ProvisionRequest provisionRequest) {
        log.trace("Provision request:${provisionRequest.toString()}")
        handleAsyncClientRequirement(provisionRequest.plan, provisionRequest.acceptsIncomplete)

        if (StringUtils.contains(provisionRequest.parameters, "parent_reference")) {
            checkParent(provisionRequest)
        }

        ServiceInstance instance = provisioningPersistenceService.createServiceInstance(provisionRequest)
        ServiceProvider serviceProvider = serviceProviderLookup.findServiceProvider(provisionRequest.plan)
        Context context = ServiceContextHelper.convertFrom(provisionRequest.serviceContext)
        if (provisionRequest.serviceContext && serviceProvider instanceof CloudFoundryContextRestrictedOnly && !(context instanceof CloudFoundryContext)) {
            ErrorCode.CLOUDFOUNDRY_CONTEXT_REQUIRED.throwNew()
        }

        ProvisionResponse provisionResponse = serviceProvider.provision(provisionRequest)
        if (serviceProvider instanceof ExtensionProvider) {
            provisionResponse.extensions = serviceProvider.buildExtensions()
        }

        if (serviceProvider instanceof SensitiveParameterProvider) {
            instance.parameters = null
        }

        instance = provisioningPersistenceService.updateServiceInstanceCompletion(instance, !provisionResponse.isAsync)
        provisioningPersistenceService.updateServiceDetails(provisionResponse.details, instance)
        return provisionResponse
    }

    private static void handleAsyncClientRequirement(Plan plan, boolean acceptsIncomplete) {
        if ((plan.service.asyncRequired || plan.asyncRequired) && !acceptsIncomplete) {
            ErrorCode.ASYNC_REQUIRED.throwNew()
        }
    }

    DeprovisionResponse deprovision(DeprovisionRequest deprovisionRequest) {
        log.trace("DeprovisionRequest request:${deprovisionRequest.toString()}")
        handleAsyncClientRequirement(deprovisionRequest.serviceInstance.plan, deprovisionRequest.acceptsIncomplete)

        ServiceProvider serviceProvider = serviceProviderLookup.findServiceProvider(deprovisionRequest.serviceInstance.plan)
        checkActiveChildren(deprovisionRequest)

        DeprovisionResponse response = serviceProvider.deprovision(deprovisionRequest)
        if (!response.isAsync) {
            provisioningPersistenceService.markServiceInstanceAsDeleted(deprovisionRequest.serviceInstance)
        }
        return response
    }

    private void checkParent(ProvisionRequest provisionRequest) {
        if (!provisioningPersistenceService.findParentServiceInstance(provisionRequest.parameters)) {
            ErrorCode.PARENT_SERVICE_INSTANCE_NOT_FOUND.throwNew()
        } else {
            ServiceInstance parentServiceInstance = provisioningPersistenceService.findParentServiceInstance(provisionRequest.parameters)
            ServiceProvider parentServiceProvider = serviceProviderLookup.findServiceProvider(parentServiceInstance.plan)
            if (!(parentServiceProvider instanceof ParentServiceProvider)) {
                ErrorCode.NOT_A_PARENT_PROVIDER.throwNew()
            } else if (parentServiceProvider instanceof ParentServiceProvider && parentServiceProvider.isFull(parentServiceInstance)) {
                ErrorCode.PARENT_SERVICE_FULL.throwNew()
            }
        }
    }

    private void checkActiveChildren(DeprovisionRequest deprovisionRequest) {
        ServiceProvider serviceProvider = serviceProviderLookup.findServiceProvider(deprovisionRequest.serviceInstance.plan)
        if (serviceProvider instanceof ParentServiceProvider && serviceProvider.hasActiveChildren(deprovisionRequest.serviceInstance)) {
            ErrorCode.CHILDREN_SERVICE_INSTANCES_ACTIVE.throwNew()
        }
    }
}
