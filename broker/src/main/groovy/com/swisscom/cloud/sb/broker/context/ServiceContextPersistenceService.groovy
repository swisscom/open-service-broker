package com.swisscom.cloud.sb.broker.context

import com.swisscom.cloud.sb.broker.model.ServiceContext
import com.swisscom.cloud.sb.broker.model.ServiceContextDetail
import com.swisscom.cloud.sb.broker.model.repository.ServiceContextDetailRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceContextRepository
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
class ServiceContextPersistenceService {

    public static final String CF_SPACE_GUID = "space_guid"
    public static final String CF_ORGANIZATION_GUID = "organization_guid"
    public static final String KUBERNETES_NAMESPACE = "namespace"
    public static final String PLATFORM = "platform"

    @Autowired
    private ServiceContextDetailRepository serviceContextDetailRepository
    @Autowired
    private ServiceContextRepository serviceContextRepository

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository

    ServiceContext create(Context context) {
        if (!context) {
            return
        }

        if (context instanceof CloudFoundryContext) {
            return processCloudFoundryContext(context as CloudFoundryContext)
        } else if (context instanceof KubernetesContext) {
            return processKubernetesContext(context as KubernetesContext)
        }
    }

    Context convertFrom(List<ServiceContextDetail> contextDetails) {
        def platform = contextDetails.find { it -> it.key == PLATFORM }?.value
        if (platform == CloudFoundryContext.CLOUD_FOUNDRY_PLATFORM) {
            String organizationGuid = contextDetails.find { it -> it.key == CF_ORGANIZATION_GUID }.value
            String spaceGuid = contextDetails.find { it -> it.key == CF_SPACE_GUID }.value
            return new CloudFoundryContext(organizationGuid, spaceGuid)
        } else {
            String namespace = contextDetails.find { it -> it.key == KUBERNETES_NAMESPACE }.value
            return new KubernetesContext(namespace)
        }
    }

    private ServiceContext processCloudFoundryContext(CloudFoundryContext context) {
        def serviceContext = new ServiceContext(platform: CloudFoundryContext.CLOUD_FOUNDRY_PLATFORM)
        serviceContextRepository.save(serviceContext)

        def contextDetails = [] as Set<ServiceContextDetail>
        contextDetails << createServiceContextDetailRecord(CF_ORGANIZATION_GUID, context.organizationGuid, serviceContext)
        contextDetails << createServiceContextDetailRecord(CF_SPACE_GUID, context.spaceGuid, serviceContext)
        serviceContextDetailRepository.flush()

        serviceContext.details.addAll(contextDetails)
        serviceContextRepository.merge(serviceContext)
        serviceContextRepository.flush()

        return serviceContext
    }

    private ServiceContext processKubernetesContext(KubernetesContext context) {
        def serviceContext = new ServiceContext(platform: KubernetesContext.KUBERNETES_PLATFORM)
        serviceContextRepository.save(serviceContext)

        def contextDetails = [] as Set<ServiceContextDetail>
        contextDetails << createServiceContextDetailRecord(KUBERNETES_NAMESPACE, context.namespace, serviceContext)
        serviceContextDetailRepository.flush()

        serviceContext.details.addAll(contextDetails)
        serviceContextRepository.merge(serviceContext)
        return serviceContext
    }

    private ServiceContextDetail createServiceContextDetailRecord(String key, String value, ServiceContext serviceContext) {
        return serviceContextDetailRepository.save(new ServiceContextDetail(key: key, value: value, serviceContext: serviceContext))
    }

}
