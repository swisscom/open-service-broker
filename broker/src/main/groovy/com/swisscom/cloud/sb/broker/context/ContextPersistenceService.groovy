package com.swisscom.cloud.sb.broker.context

import com.swisscom.cloud.sb.broker.model.ServiceContext
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

    List<ServiceContext> create(Context context) {
        if (!context) {
            return
        }

        if (context instanceof CloudFoundryContext) {
            return processCloudFoundryContext(context as CloudFoundryContext)
        } else if (context instanceof KubernetesContext) {
            return processKubernetesContext(context as KubernetesContext)
        }
    }

    private List<ServiceContext> processCloudFoundryContext(CloudFoundryContext context) {
        def contexts = []
        contexts << createServiceContextRecord(PLATFORM, CloudFoundryContext.CLOUD_FOUNDRY_PLATFORM)
        contexts << createServiceContextRecord(CF_ORGANIZATION_GUID, context.organizationGuid)
        contexts << createServiceContextRecord(CF_SPACE_GUID, context.spaceGuid)
        contextRepository.flush()
        return contexts
    }

    private List<ServiceContext> processKubernetesContext(KubernetesContext context) {
        def contexts = []
        contexts << createServiceContextRecord(PLATFORM, KubernetesContext.KUBERNETES_PLATFORM)
        contexts << createServiceContextRecord(KUBENETES_NAMESPACE, context.namespace)
        contextRepository.flush()
        return contexts
    }

    private ServiceContext createServiceContextRecord(String key, String value) {
        def sc = new ServiceContext()
        sc.key = key
        sc.value = value
        sc = contextRepository.save(sc)
        return sc
    }

}
