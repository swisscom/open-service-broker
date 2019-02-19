package com.swisscom.cloud.sb.broker.services.genericserviceprovider

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.swisscom.cloud.sb.broker.cfapi.converter.MetadataJsonHelper
import com.swisscom.cloud.sb.broker.cfapi.dto.DashboardClientDto
import com.swisscom.cloud.sb.broker.cfapi.dto.SchemasDto
import com.swisscom.cloud.sb.broker.model.CFServiceMetadata
import com.swisscom.cloud.sb.broker.model.CFServicePermission
import com.swisscom.cloud.sb.broker.model.Parameter
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.PlanMetadata
import com.swisscom.cloud.sb.broker.model.Tag
import com.swisscom.cloud.sb.broker.servicedefinition.dto.ParameterDto
import com.swisscom.cloud.sb.broker.servicedefinition.dto.PlanDto

import com.swisscom.cloud.sb.broker.servicedefinition.dto.ServiceDto
import org.springframework.cloud.servicebroker.model.DashboardClient
import org.springframework.cloud.servicebroker.model.ServiceDefinition
import org.springframework.stereotype.Component

import static com.swisscom.cloud.sb.broker.cfapi.converter.MetadataJsonHelper.getValue

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.ANY)
@Component
class ProxyService extends ServiceDefinition {

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

    ProxyService(){

    }

    @JsonCreator
    ProxyService(@JsonProperty("id") String id, @JsonProperty("guid") String guid, @JsonProperty("name") String name,
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

    ServiceDto convertToServiceDto(){
        ServiceDto serviceDto = new ServiceDto()
        serviceDto.guid = this.guid
        serviceDto.internalName = this.internalName
        serviceDto.serviceProviderClass = this.serviceProviderClass
        serviceDto.displayIndex = this.displayIndex
        serviceDto.asyncRequired = this.asyncRequired
        serviceDto.plans = this.convertPlans(this.plansList.sort { it.displayIndex })
        serviceDto.id = this.id
        serviceDto.name = this.name
        serviceDto.description = this.description
        serviceDto.metadata = convertMetadata(this)
        serviceDto.tags = this.tagsList.collect { Tag t -> t.tag }
        serviceDto.requires = this.permissions.collect { CFServicePermission p -> p.permission }
        serviceDto.dashboard_client = new DashboardClientDto()
        serviceDto.dashboard_client.id = this.dashboardClientId
        serviceDto.dashboard_client.secret = this.dashboardClientSecret
        serviceDto.dashboard_client.redirect_uri = this.dashboardClientRedirectUri
        serviceDto.bindable = this.bindable
        serviceDto.active = this.active
        serviceDto.plan_updateable = this.plan_updateable
        serviceDto.bindingsRetrievable = this.bindingsRetrievable
        serviceDto.instancesRetrievable = this.instancesRetrievable

        return serviceDto
    }

    private Map<String, Object> convertMetadata(ProxyService service) {
        Map<String, Object> result = [:]
        service.metadataList.each { result[it.key] = MetadataJsonHelper.getValue(it.type, it.value) }
        result
    }

    List<PlanDto> convertPlans(List<Plan> planList){
        List<PlanDto> plans = []
        planList.each {plan ->
            PlanDto planDto = new PlanDto()
            planDto.guid = plan.guid
            planDto.templateId = plan.templateUniqueIdentifier
            planDto.templateVersion = plan.templateVersion
            planDto.internalName = plan.internalName
            planDto.serviceProviderClass = plan.serviceProviderClass
            planDto.displayIndex = plan.displayIndex
            planDto.asyncRequired = plan.asyncRequired
            planDto.maxBackups = plan.maxBackups
            planDto.parameters = this.convertParameter(plan.parameters)
            planDto.containerParams = this.convertParameter(plan.parameters)
            planDto.id = plan.id
            planDto.active = plan.active
            planDto.description = plan.description
            planDto.free = plan.free
            planDto.metadata = this.convertPlanMetadata(plan.metadata)
            planDto.schemas = new SchemasDto(plan)
        }
        plans
    }

    List<ParameterDto> convertParameter(Set<Parameter> parameters){
        List<ParameterDto> parameterDtoList = []
        parameters.each { parameter ->
            ParameterDto parameterDto = new ParameterDto()
            parameterDto.name = parameter.name
            parameterDto.value = parameter.value
            parameterDto.template = parameter.template
            parameterDtoList.add(parameterDto)
        }
        parameterDtoList
    }

    Map<String, Object> convertPlanMetadata(Set<PlanMetadata> planMetadatas){
        Map<String, Object> result = [:]
        planMetadatas.each { result[it.key] = getValue(it.type, it.value) }
        result
    }
}
