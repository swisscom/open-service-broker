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

package com.swisscom.cloud.sb.broker.util.servicecontext

import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.model.ServiceContext
import org.springframework.cloud.servicebroker.model.CloudFoundryContext
import org.springframework.cloud.servicebroker.model.Context
import org.springframework.cloud.servicebroker.model.KubernetesContext

class ServiceContextHelper {

    public static final String CF_SPACE_GUID = "space_guid"
    public static final String CF_ORGANIZATION_GUID = "organization_guid"
    public static final String KUBERNETES_NAMESPACE = "namespace"

    static Context convertFrom(ServiceContext serviceContext) {
        if (!serviceContext) {
            return null
        }
        def platform = serviceContext.platform
        switch (platform) {
            case { it == CloudFoundryContext.CLOUD_FOUNDRY_PLATFORM }:
                String organizationGuid = serviceContext.details.find { it -> it.key == CF_ORGANIZATION_GUID }.value
                String spaceGuid = serviceContext.details.find { it -> it.key == CF_SPACE_GUID }.value
                return CloudFoundryContext.builder().organizationGuid(organizationGuid).spaceGuid(spaceGuid).build();
            case { it == KubernetesContext.KUBERNETES_PLATFORM }:
                String namespace = serviceContext.details.find { it -> it.key == KUBERNETES_NAMESPACE }.value
                return KubernetesContext.builder().namespace(namespace).build()
            default:
                return new Context(serviceContext.platform, serviceContext.details.collectEntries { d -> [(d.key): (d.value)] })
        }
    }
}
