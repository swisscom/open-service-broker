package com.swisscom.cloud.sb.broker.context

import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.ServiceContext
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.ContextRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.CloudFoundryContext
import org.springframework.cloud.servicebroker.model.Context
import org.springframework.cloud.servicebroker.model.KubernetesContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Slf4j
@Service
@Transactional
@CompileStatic
class ContextPersistenceService {

    public static final String CF_SPACE_GUID = "space_guid"
    public static final String CF_ORGANIZATION_GUID = "organization_guid"
    public static final String KUBENETES_NAMESPACE = "namespace"
    public static final String PLATFORM = "platform"

    @Autowired
    private ContextRepository contextRepository

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository

    void createUpdateContext(String serviceInstanceId, Context context) {
        if (!context) {
            return
        }

        ServiceInstance serviceInstance = getAndCheckServiceInstance(serviceInstanceId)
        if (context instanceof CloudFoundryContext) {
            processCloudFoundryContext(serviceInstance, context as CloudFoundryContext)
        } else if (context instanceof KubernetesContext) {
            processKubernetesContext(serviceInstance, context as KubernetesContext)
        }
    }

    private void processCloudFoundryContext(ServiceInstance serviceInstance, CloudFoundryContext context) {
        def platform = contextRepository.findByKeyAndServiceInstance(PLATFORM, serviceInstance)
        if (!platform) {
            createServiceContextRecord(serviceInstance, PLATFORM, CloudFoundryContext.CLOUD_FOUNDRY_PLATFORM)
        }
        def organizationGuid = contextRepository.findByKeyAndServiceInstance(CF_ORGANIZATION_GUID, serviceInstance)
        if (!organizationGuid) {
            createServiceContextRecord(serviceInstance, CF_ORGANIZATION_GUID, context.organizationGuid)
        }
        def spaceGuid = contextRepository.findByKeyAndServiceInstance(CF_SPACE_GUID, serviceInstance)
        if (!spaceGuid) {
            createServiceContextRecord(serviceInstance, CF_SPACE_GUID, context.spaceGuid)
        }
        contextRepository.flush()
    }

    private void processKubernetesContext(ServiceInstance serviceInstance, KubernetesContext context) {
        def platform = contextRepository.findByKeyAndServiceInstance(PLATFORM, serviceInstance)
        if (!platform) {
            createServiceContextRecord(serviceInstance, PLATFORM, KubernetesContext.KUBERNETES_PLATFORM)
        }
        def namespace = contextRepository.findByKeyAndServiceInstance(KUBENETES_NAMESPACE, serviceInstance)
        if (!namespace) {
            createServiceContextRecord(serviceInstance, KUBENETES_NAMESPACE, context.namespace)
        }
        contextRepository.flush()
    }

    private ServiceContext createServiceContextRecord(ServiceInstance serviceInstance, String key, String value) {
        def sc = new ServiceContext()
        sc.key = key
        sc.value = value
        sc.serviceInstance = serviceInstance
        sc = contextRepository.save(sc)

        serviceInstance.contexts << sc
        serviceInstanceRepository.save(serviceInstance)

        return sc
    }

    protected ServiceInstance getAndCheckServiceInstance(String serviceInstanceId) {
        ServiceInstance serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceId)
        if (!serviceInstance) {
            ErrorCode.SERVICE_INSTANCE_NOT_FOUND.throwNew("ID = ${serviceInstanceId}")
        }
        if (serviceInstance.deleted) {
            ErrorCode.SERVICE_INSTANCE_DELETED.throwNew("ID = ${serviceInstanceId}")
        }
        return serviceInstance
    }

}
