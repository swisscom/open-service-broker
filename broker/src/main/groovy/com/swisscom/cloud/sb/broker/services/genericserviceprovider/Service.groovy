package com.swisscom.cloud.sb.broker.services.genericserviceprovider

import com.swisscom.cloud.sb.broker.model.CFServiceMetadata
import com.swisscom.cloud.sb.broker.model.CFServicePermission
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.Tag
import org.springframework.cloud.servicebroker.model.DashboardClient
import org.springframework.cloud.servicebroker.model.ServiceDefinition


class Service extends ServiceDefinition{

    String guid
    String name
    String description
    Boolean bindable
    String internalName
    String serviceProviderClass
    int displayIndex
    Boolean plan_updateable
    Boolean asyncRequired
    Boolean active = true

    String dashboardClientId
    String dashboardClientSecret
    String dashboardClientRedirectUri

    Set<Tag> tagsList = []
    Set<Plan> plansList = []
    Set<CFServiceMetadata> metadataList = []
    Set<CFServicePermission> permissions = []

    Boolean instancesRetrievable
    Boolean bindingsRetrievable

    Service(String id, String guid, String name, String description, Boolean bindable,
            List<org.springframework.cloud.servicebroker.model.Plan> plans, String internalName,
            List<String> tags, Map<String, Object> metadata, List<String> requires, String dashboardClientId,
            String dashboardClientSecret, String dashboardClientRedirectUri, Boolean updateable,
            String serviceProviderClass, Integer displayIndex, Boolean asyncRequired, Boolean active,
            Set<Tag> tagsList, Set<Plan> plansList, Set<CFServiceMetadata> metadataList,
            Set<CFServicePermission> permissions, Boolean instancesRetrievable, Boolean bindingsRetrievable){
        super(id, name, description, bindable, updateable, plans, tags, metadata, requires, new DashboardClient(dashboardClientId, dashboardClientSecret, dashboardClientRedirectUri))
        this.guid = guid
        this.bindable = bindable
        this.internalName = internalName
        this.plan_updateable = updateable
        this.serviceProviderClass = serviceProviderClass
        this.displayIndex = displayIndex
        this.asyncRequired = asyncRequired
        this.active = active
        this.tagsList = tagsList
        this.plansList = plansList
        this.metadataList = metadataList
        this.permissions = permissions
        this.instancesRetrievable = instancesRetrievable
        this.bindingsRetrievable = bindingsRetrievable
        this.dashboardClientId = dashboardClientId
        this.dashboardClientSecret = dashboardClientSecret
        this.dashboardClientRedirectUri = dashboardClientRedirectUri
    }
}
