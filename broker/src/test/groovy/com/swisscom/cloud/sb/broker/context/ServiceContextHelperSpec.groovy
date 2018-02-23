package com.swisscom.cloud.sb.broker.context

import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.model.ServiceContext
import com.swisscom.cloud.sb.broker.model.ServiceContextDetail
import com.swisscom.cloud.sb.broker.util.servicecontext.ServiceContextHelper
import org.springframework.cloud.servicebroker.model.CloudFoundryContext
import org.springframework.cloud.servicebroker.model.KubernetesContext
import spock.lang.Specification

class ServiceContextHelperSpec extends Specification {

    def "Convert CF context"() {
        setup:
        def serviceContext = new ServiceContext()
        serviceContext.platform = "cloudfoundry"
        serviceContext.details << new ServiceContextDetail(key: ServiceContextHelper.CF_ORGANIZATION_GUID, value: "org_id")
        serviceContext.details << new ServiceContextDetail(key: ServiceContextHelper.CF_SPACE_GUID, value: "space_id")

        when:
        def context = ServiceContextHelper.convertFrom(serviceContext) as CloudFoundryContext
        then:
        assert context != null
        assert context instanceof CloudFoundryContext
        assert context.organizationGuid == "org_id"
        assert context.spaceGuid == "space_id"
    }

    def "Convert Kubernetes context"() {
        setup:
        def serviceContext = new ServiceContext()
        serviceContext.platform = "kubernetes"
        serviceContext.details << new ServiceContextDetail(key: ServiceContextHelper.KUBERNETES_NAMESPACE, value: "my_namespace")

        when:
        def context = ServiceContextHelper.convertFrom(serviceContext) as KubernetesContext

        then:
        assert context != null
        assert context instanceof KubernetesContext
        assert context.namespace == "my_namespace"
    }

    def "Convert Unknown context"() {
        setup:
        def serviceContext = new ServiceContext()
        serviceContext.platform = "unknown"
        serviceContext.details << new ServiceContextDetail(key: ServiceContextHelper.KUBERNETES_NAMESPACE, value: "my_namespace")

        when:
        ServiceContextHelper.convertFrom(serviceContext) as KubernetesContext

        then:
        thrown(ServiceBrokerException)
    }
}

