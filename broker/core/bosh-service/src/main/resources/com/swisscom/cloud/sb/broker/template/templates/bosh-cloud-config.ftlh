<#-- @ftlvariable name="cloudConfig" type="com.swisscom.cloud.sb.broker.services.bosh.client.BoshCloudConfig" -->
<#if (cloudConfig.vmTypes?size > 0)>
vm_types:
    <#list cloudConfig.vmTypes as vmType>
  - name: "${vmType.name}"
    </#list>
</#if>
<#if (cloudConfig.diskTypes?size > 0)>
disk_types:
    <#list cloudConfig.diskTypes as diskType>
  - name: "${diskType.name}"
    disk_size: ${diskType.size}
    </#list>
</#if>
<#if (cloudConfig.networks?size > 0)>
networks:
    <#list cloudConfig.networks as network>
  - name: "${network.name}"
    type: ${network.type}
    <#if (network.subnets?size > 0)>
    subnets:
        <#list network.subnets as subnet>
      - azs: [${subnet.availabilityZones?join(", ")}]
        dns: [${subnet.dns?join(", ")}]
        range: ${subnet.range}
        gateway: ${subnet.gateway}
        static: [${subnet.static?join(", ")}]
        reserved: [${subnet.reserved?join(", ")}]
        </#list>
    </#if>
    </#list>
</#if>