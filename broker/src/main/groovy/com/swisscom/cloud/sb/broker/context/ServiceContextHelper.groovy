package com.swisscom.cloud.sb.broker.context

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
        if (platform == CloudFoundryContext.CLOUD_FOUNDRY_PLATFORM) {
            String organizationGuid = serviceContext.details.find { it -> it.key == CF_ORGANIZATION_GUID }.value
            String spaceGuid = serviceContext.details.find { it -> it.key == CF_SPACE_GUID }.value
            return new CloudFoundryContext(organizationGuid, spaceGuid)
        } else if (platform == KubernetesContext.KUBERNETES_PLATFORM) {
            String namespace = serviceContext.details.find { it -> it.key == KUBERNETES_NAMESPACE }.value
            return new KubernetesContext(namespace)
        } else {
            throw new IllegalArgumentException("Unknown ServiceContext platform")
        }
    }

}
