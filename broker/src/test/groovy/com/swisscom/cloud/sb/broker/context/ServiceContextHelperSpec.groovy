/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

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

