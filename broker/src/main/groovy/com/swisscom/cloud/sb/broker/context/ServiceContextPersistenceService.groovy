package com.swisscom.cloud.sb.broker.context

import com.swisscom.cloud.sb.broker.model.ServiceContext
import com.swisscom.cloud.sb.broker.model.ServiceContextDetail
import com.swisscom.cloud.sb.broker.model.repository.ServiceContextDetailRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceContextRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.util.servicecontext.ServiceContextHelper
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

    @Autowired
    private ServiceContextDetailRepository serviceContextDetailRepository

    @Autowired
    private ServiceContextRepository serviceContextRepository

    ServiceContext findOrCreate(Context context) {
        if (!context) {
            return
        }

        if (context instanceof CloudFoundryContext) {
            return findOrCreateCloudFoundryContext(context as CloudFoundryContext)
        } else if (context instanceof KubernetesContext) {
            return findOrCreateKubernetesContext(context as KubernetesContext)
        }
    }

    private ServiceContext findOrCreateCloudFoundryContext(CloudFoundryContext context) {
        def existingServiceContext = serviceContextRepository.findCloudFoundryServiceContext(context.organizationGuid, context.spaceGuid)
        if (existingServiceContext) {
            return existingServiceContext
        }
        return createCloudFoundryContext(context)
    }

    private ServiceContext createCloudFoundryContext(CloudFoundryContext context) {
        def serviceContext = new ServiceContext(platform: CloudFoundryContext.CLOUD_FOUNDRY_PLATFORM)
        serviceContextRepository.saveAndFlush(serviceContext)

        def contextDetails = [] as Set<ServiceContextDetail>
        contextDetails << createServiceContextDetailRecord(ServiceContextHelper.CF_ORGANIZATION_GUID, context.organizationGuid, serviceContext)
        contextDetails << createServiceContextDetailRecord(ServiceContextHelper.CF_SPACE_GUID, context.spaceGuid, serviceContext)
        serviceContextDetailRepository.flush()

        serviceContext.details.addAll(contextDetails)
        serviceContextRepository.merge(serviceContext)
        serviceContextRepository.flush()

        return serviceContext
    }

    private ServiceContext findOrCreateKubernetesContext(KubernetesContext context) {
        def existingServiceContext = serviceContextRepository.findKubernetesServiceContext(context.namespace)
        if (existingServiceContext) {
            return existingServiceContext
        }
        return createKubernetesContext(context)
    }

    private ServiceContext createKubernetesContext(KubernetesContext context) {
        def serviceContext = new ServiceContext(platform: KubernetesContext.KUBERNETES_PLATFORM)
        serviceContextRepository.saveAndFlush(serviceContext)

        def contextDetails = [] as Set<ServiceContextDetail>
        contextDetails << createServiceContextDetailRecord(ServiceContextHelper.KUBERNETES_NAMESPACE, context.namespace, serviceContext)
        serviceContextDetailRepository.flush()

        serviceContext.details.addAll(contextDetails)
        serviceContextRepository.merge(serviceContext)
        return serviceContext
    }

    private ServiceContextDetail createServiceContextDetailRecord(String key, String value, ServiceContext serviceContext) {
        return serviceContextDetailRepository.saveAndFlush(new ServiceContextDetail(key: key, value: value, serviceContext: serviceContext))
    }
}
