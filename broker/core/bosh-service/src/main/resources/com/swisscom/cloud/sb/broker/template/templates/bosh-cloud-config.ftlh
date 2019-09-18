<#-- @ftlvariable name="cloudConfig" type="com.swisscom.cloud.sb.broker.services.bosh.client.BoshCloudConfig" -->
<#if (cloudConfig.availabilityZones?size > 0)>
azs:
  <#list cloudConfig.availabilityZones as availabilityZone>
- name: ${availabilityZone.name}
  cpi: ${availabilityZone.cpi}
  cloud_properties:
    datacenters:
    - name: ${availabilityZone.datacenter.name}
      clusters:
      - ${availabilityZone.datacenter.cluster}: {}
  </#list>
</#if>
<#if !(cloudConfig.compilation.isEmpty())>
compilation:
  vm_type: compilation
  network: ${cloudConfig.compilation.network.name}
  az: ${cloudConfig.compilation.availabilityZone.name}
  reuse_compilation_vms: ${cloudConfig.compilation.areCompilationVmsReused()?c}
  workers: ${cloudConfig.compilation.numberOfWorkers}
  env:
    ${cloudConfig.compilation.environmentAsYaml?replace("\n", "\n    ")?trim}
</#if>
<#if (cloudConfig.networks?size > 0)>
networks:
    <#list cloudConfig.networks as network>
- name: ${network.name}
  type: ${network.type.value()}
    <#if (network.subnets?size > 0)>
  subnets:
        <#list network.subnets as subnet>
  - name: ${subnet.name}
    az: ${subnet.availabilityZones?first}
    cloud_properties:
      ${subnet.cloudPropertiesAsYaml?replace("\n", "\n      ")?trim}
    dns:<#list subnet.dns as dns>
    - ${dns}</#list>
    gateway: ${subnet.gateway}
    range: ${subnet.range}
          <#if (subnet.static?size > 0) >
    static:<#list subnet.static as static>
    - ${static.first} - ${static.last}
            </#list>
          </#if>
          <#if (subnet.reserved?size > 0) >
    reserved:<#list subnet.reserved as reserved>
    - ${reserved.first} - ${reserved.last}</#list>
          </#if>
    </#list>
    </#if>
    </#list>
</#if>
<#if (cloudConfig.diskTypes?size > 0)>
disk_types:
  <#list cloudConfig.diskTypes as diskType>
- name: ${diskType.name}
  disk_size: ${diskType.getSizeInMegabytes()?c}
  </#list>
</#if>
<#if (cloudConfig.vmTypes?size > 0)>
vm_types:
  <#list cloudConfig.vmTypes as vmType>
- name: ${vmType.name}
  cloud_properties:
    instance_type: ${vmType.getInstanceType()}
    cpu: ${vmType.getNumberOfCpus()}
    ram: ${vmType.getRamSizeInMegabytes()?c}
    disk: ${vmType.getDiskSizeInMegabytes()?c}
  </#list>
</#if>