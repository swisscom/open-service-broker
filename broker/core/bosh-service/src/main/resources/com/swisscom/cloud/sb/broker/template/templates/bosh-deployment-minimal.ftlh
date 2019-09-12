<#-- @ftlvariable name="deploy" type="com.swisscom.cloud.sb.broker.services.bosh.client.BoshDeploymentRequest" -->
name: "d-${deploy.name}"
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
    instances: ${instanceGroup.numberOfInstances}
    vm_type: ${instanceGroup.vmType}
    stemcell: ${instanceGroup.stemcell.name}
    networks:
    <#list instanceGroup.networks as network>
      - name: "${network}"
    </#list>
    jobs:
    <#list instanceGroup.jobs as job>
      - name: ${job.name}
    </#list>
  </#list>
</#if>
update:
  canaries: ${deploy.update.numberOfCanaries}
  max_in_flight: ${deploy.update.maxInFlight}
  serial: ${deploy.update.serial?string("true", "false")}
  canary_watch_time: ${deploy.update.canaryWatchTime}
  update_watch_time: ${deploy.update.updateWatchTime}