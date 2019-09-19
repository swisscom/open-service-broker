<#-- @ftlvariable name="bosh_prefix" type="java.lang.String" -->
<#-- @ftlvariable name="bosh_info" type="com.swisscom.cloud.sb.broker.services.bosh.client.BoshInfo" -->
<#-- @ftlvariable name="deploy" type="com.swisscom.cloud.sb.broker.services.bosh.client.BoshDeploymentRequest" -->
name: "${bosh_prefix}-${deploy.name}"
director_uuid: '${bosh_info.uuid}'
<#if (deploy.releases?size > 0) >
releases:
  <#list deploy.releases as release>
  - name: ${release.name}
    version: ${release.latest.version}
  </#list>
</#if>
<#if (deploy.stemcells?size > 0) >
stemcells:
  <#list deploy.stemcells as stemcell>
  - alias: ${stemcell.name}
    os: ${stemcell.operatingSystem}
    version: ${stemcell.version}
  </#list>
</#if>
<#if (deploy.instanceGroups?size > 0) >
instance_groups:
  <#list deploy.instanceGroups as instanceGroup>
  - name: ${instanceGroup.name}
    azs: [${instanceGroup.availabilityZones?join(", ")}]
    <#if (instanceGroup.environmentProperties?size > 0)>
    env:
      ${instanceGroup.environmentPropertiesAsYaml?replace("\n", "\n      ")?trim}
    </#if>
    instances: ${instanceGroup.numberOfInstances}
    vm_type: ${instanceGroup.vmType}
    persistent_disk_type: ${instanceGroup.persistentDiskType}
    stemcell: ${instanceGroup.stemcell.name}
    networks:
    <#list instanceGroup.networks as network>
      - name: ${network}
        default:
        - dns
        - gateway
    </#list>
    jobs:
    <#list instanceGroup.jobs as job>
      - name: ${job.name}
        release: ${job.release.name}
        <#if (job.consumes?size > 0)>
        consumes:
          <#list job.consumes as consume>
          ${consume}:
            from: ${consume}
            </#list>
        </#if>
        <#if (job.provides?size > 0)>
        provides:
          <#list job.provides as provide>
          ${provide}:
            as: ${provide}
            </#list>
        </#if>
        <#if (job.properties?size > 0) >
        properties:
          ${job.propertiesAsYaml?replace("\n", "\n          ")?trim}
        </#if>
    </#list>
  </#list>
</#if>
update:
  canaries: ${deploy.update.numberOfCanaries}
  max_in_flight: ${deploy.update.maxInFlight}
  serial: ${deploy.update.serial?string("true", "false")}
  canary_watch_time: ${deploy.update.canaryWatchTime}
  update_watch_time: ${deploy.update.updateWatchTime}
<#if (deploy.variables?size >0) >
variables:
    <#list deploy.variables as variable>
    - name: ${variable.name}
      type: ${variable.type.value()}
      options:
          <#list variable.options as key,value>
        ${key}: ${value}
          </#list>
      <#if variable.type.name() == "CERTIFICATE_CA">
        is_ca: true
      </#if>
    </#list>
</#if>