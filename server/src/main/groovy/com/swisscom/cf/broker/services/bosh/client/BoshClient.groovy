package com.swisscom.cf.broker.services.bosh.client

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.google.common.base.Optional
import com.google.common.base.Preconditions
import com.google.common.base.Strings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.swisscom.cf.broker.services.bosh.BoshConfig
import com.swisscom.cf.broker.services.bosh.dto.*
import com.swisscom.cf.broker.util.MutexFactory
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.yaml.snakeyaml.Yaml

@Log4j
@CompileStatic
class BoshClient {
    private final BoshConfig boshConfig
    private final BoshRestClient boshRestClient
    private final MutexFactory mutexFactory

    public static final String VM_TYPES = 'vm_types'

    BoshClient(BoshRestClient boshRestClient, MutexFactory mutexFactory) {
        this.boshConfig = boshRestClient.getBoshConfig()
        this.boshRestClient = boshRestClient
        this.mutexFactory = mutexFactory
    }

    CloudConfigContainerDto fetchCloudConfig() {
        Collection<CloudConfigContainerDto> result = (Collection<CloudConfigContainerDto>) new Gson().fromJson(boshRestClient.getCloudConfig(), new TypeToken<Collection<CloudConfigContainerDto>>() {
        }.getType())
        return result.first()
    }

    void updateCloudConfig(String cloudConfig) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(cloudConfig))
        log.debug("Updating cloud config: ${cloudConfig}")
        synchronized (createMutexBasedOnClassnameAndBoshUrl()) {
            boshRestClient.postCloudConfig(cloudConfig)
        }
    }

    void addOrUpdateVmInCloudConfig(String vm, String instanceType, String affinityGroup) {
        log.debug("AddOrUpdate VM in cloud config-> vm:${vm}, instanceType:${instanceType}, affinitiyGroup:${affinityGroup}")
        processCloudConfig({ Collection<VmDto> input -> addOrUpdateVm(input, vm, instanceType, affinityGroup) })
    }

    private void processCloudConfig(Closure<Collection<VmDto>> closure) {
        synchronized (createMutexBasedOnClassnameAndBoshUrl()) {
            Map cloudConfigYml = parseMapFromYml(fetchCloudConfig().properties)
            def vms = parseVMSection(cloudConfigYml)
            cloudConfigYml[VM_TYPES] = closure(vms)
            updateCloudConfig(getYmlAsString(cloudConfigYml))
        }
    }

    void removeVmInCloudConfig(String vmName) {
        log.debug("Remove VM in cloud config-> vm:${vmName}")
        processCloudConfig({ Collection<VmDto> input -> removeVm(input, vmName) })
    }

    private Object createMutexBasedOnClassnameAndBoshUrl() {
        return mutexFactory.getNamedMutex("${BoshClient.class.simpleName}_${boshConfig.boshDirectorBaseUrl}")
    }

    /**
     * Creates a new deployment
     *
     * @param yml Deployment manifest
     * @return String indicating task id that is created a result of deployment.
     */
    String postDeployment(String yml) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(yml))
        try {
            new Yaml().load(yml)
        } catch (Exception e) {
            throw new RuntimeException("Deployment needs a valid yml file: ${yml}")
        }
        return boshRestClient.postDeployment(yml)
    }

    /**
     * Deletes a deployment
     *
     * @param id deploymentId
     * @return String indicating task id that is created.
     */
    Optional<String> deleteDeploymentIfExists(String id) {
        try {
            return Optional.of(boshRestClient.deleteDeployment(id))
        } catch (BoshResourceNotFoundException e) {
            log.warn("Bosh deployment ${id} not found")
            return Optional.absent()
        }
    }

    TaskDto getTask(String id) {
        String task = boshRestClient.getTask(id)
        log.info("Bosh task:${task}")
        return new Gson().fromJson(task, TaskDto)
    }

    BoshConfig getBoshConfig() {
        return boshConfig
    }

    BoshRestClient getBoshRestClient() {
        return boshRestClient
    }

    MutexFactory getMutexFactory() {
        return mutexFactory
    }

    BoshInfoDto fetchBoshInfo() {
        def result = boshRestClient.getBoshInfo()
        return new Gson().fromJson(result, BoshInfoDto)
    }

    private static Map parseMapFromYml(String yml) {
        return (Map) new Yaml().load(yml)
    }

    private static String getYmlAsString(Map cloudConfigYml) {
        return createYmlObjectMapper().writeValueAsString(cloudConfigYml)
    }

    private static Collection<VmDto> parseVMSection(Map cloudConfigYml) {
        ObjectMapper objectMapper = createYmlObjectMapper()
        return (Collection<VmDto>) objectMapper.readValue(objectMapper.writeValueAsString(cloudConfigYml[VM_TYPES]), new TypeReference<Collection<VmDto>>() {
        })
    }

    private static ObjectMapper createYmlObjectMapper() {
        return new ObjectMapper(new YAMLFactory())
    }

    private
    static Collection<VmDto> addOrUpdateVm(Collection<VmDto> vms, String vm, String instanceType, String affinityGroup) {
        VmDto vmDto = new VmDto(name: vm,
                cloud_properties: new CloudPropertiesDto(instance_type: instanceType,
                        scheduler_hints: new SchedulerHintsDto(group: affinityGroup)))
        VmDto existingVmDto = vms.find() { it.name == vm }
        if (existingVmDto) {
            ArrayList<VmDto> result = new ArrayList<>(vms)
            result.remove(existingVmDto)
            result.add(vmDto)
            return result
        } else {
            vms.add(vmDto)
            return vms
        }
    }

    private static Collection<VmDto> removeVm(Collection<VmDto> vms, String vm) {
        VmDto existingVmDto = vms.find() { it.name == vm }
        if (!existingVmDto) {
            log.warn("VM:${vm} not found!")
            return vms
        }
        vms.remove(existingVmDto)
        return vms
    }
}
