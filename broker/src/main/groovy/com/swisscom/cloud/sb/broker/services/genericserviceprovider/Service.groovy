package com.swisscom.cloud.sb.broker.services.genericserviceprovider

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.swisscom.cloud.sb.broker.model.CFServiceMetadata
import com.swisscom.cloud.sb.broker.model.CFServicePermission
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.Tag
import org.springframework.cloud.servicebroker.model.DashboardClient
import org.springframework.cloud.servicebroker.model.ServiceDefinition

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.ANY)
class Service extends ServiceDefinition{

    String guid
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

    @JsonCreator
    Service(@JsonProperty("id") String id, @JsonProperty("guid") String guid, @JsonProperty("name") String name,
            @JsonProperty("description") String description, @JsonProperty("bindable") Boolean bindable,
            @JsonProperty("plans") List<org.springframework.cloud.servicebroker.model.Plan> plans,
            @JsonProperty("tags") List<String> tags, @JsonProperty("metadata") Map<String, Object> metadata,
            @JsonProperty("requires") List<String> requires, @JsonProperty("dashboard_client") DashboardClient dashboardClient,
            @JsonProperty("plan_updateable") Boolean updateable, @JsonProperty("internal_name") String internalName,
            @JsonProperty("service_provider_class") String serviceProviderClass,
            @JsonProperty("async_required") Boolean asyncRequired, @JsonProperty("active") Boolean active,
            @JsonProperty("tags_list") Set<Tag> tagsList, @JsonProperty("plans_list") Set<Plan> plansList,
            @JsonProperty("metadata_list") Set<CFServiceMetadata> metadataList, @JsonProperty("permissions_list") Set<CFServicePermission> permissions,
            @JsonProperty("instances_retrievable") Boolean instancesRetrievable, @JsonProperty("bindings_retrievable") Boolean bindingsRetrievable,
            @JsonProperty("dashboard_client_id") String dashboardClientId, @JsonProperty("dashboard_client_secret") String dashboardClientSecret,
            @JsonProperty("dashboard_client_redirect_uri") String dashboardClientRedirectUri, @JsonProperty("displayIndex") int displayIndex) {

        super(id, name, description, bindable, updateable, plans, tags, metadata, requires, dashboardClient)
        this.guid = guid
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

    String getInternalName() {
        return internalName
    }

    String getServiceProviderClass() {
        return serviceProviderClass
    }

    int getDisplayIndex() {
        return displayIndex
    }

    Boolean getPlan_updateable() {
        return plan_updateable
    }

    Boolean getAsyncRequired() {
        return asyncRequired
    }

    Boolean getActive() {
        return active
    }

    String getDashboardClientId() {
        return dashboardClientId
    }

    String getDashboardClientSecret() {
        return dashboardClientSecret
    }

    String getDashboardClientRedirectUri() {
        return dashboardClientRedirectUri
    }

    Set<Tag> getTagsList() {
        return tagsList
    }

    Set<Plan> getPlansList() {
        return plansList
    }

    Set<CFServiceMetadata> getMetadataList() {
        return metadataList
    }

    Set<CFServicePermission> getPermissions() {
        return permissions
    }

    Boolean getInstancesRetrievable() {
        return instancesRetrievable
    }

    Boolean getBindingsRetrievable() {
        return bindingsRetrievable
    }

    String getGuid() {
        return guid
    }


    @Override
    String toString() {
        return "Service{" +
                "guid='" + guid + '\'' +
                ", internalName='" + internalName + '\'' +
                ", serviceProviderClass='" + serviceProviderClass + '\'' +
                ", plan_updateable=" + plan_updateable +
                ", asyncRequired=" + asyncRequired +
                ", active=" + active +
                ", dashboardClientId='" + dashboardClientId + '\'' +
                ", dashboardClientSecret='" + dashboardClientSecret + '\'' +
                ", dashboardClientRedirectUri='" + dashboardClientRedirectUri + '\'' +
                ", tagsList=" + tagsList +
                ", plansList=" + plansList +
                ", metadataList=" + metadataList +
                ", permissions=" + permissions +
                ", instancesRetrievable=" + instancesRetrievable +
                ", bindingsRetrievable=" + bindingsRetrievable +
                ", displayIndex=" + displayIndex +
                '}';
    }
}
