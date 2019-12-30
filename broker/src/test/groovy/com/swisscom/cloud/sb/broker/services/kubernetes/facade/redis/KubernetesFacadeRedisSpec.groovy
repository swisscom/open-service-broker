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

package com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis

import com.swisscom.cloud.sb.broker.async.AsyncProvisioningService
import com.swisscom.cloud.sb.broker.context.CloudFoundryContextRestrictedOnly
import com.swisscom.cloud.sb.broker.model.*
import com.swisscom.cloud.sb.broker.provisioning.ProvisioningPersistenceService
import com.swisscom.cloud.sb.broker.services.common.TemplateConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.client.rest.KubernetesClient
import com.swisscom.cloud.sb.broker.services.kubernetes.config.KubernetesConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.*
import com.swisscom.cloud.sb.broker.services.kubernetes.endpoint.parameters.EndpointMapperParamsDecorated
import com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis.config.KubernetesRedisConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis.service.KubernetesRedisServiceProvider
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.constants.BaseTemplateConstants
import com.swisscom.cloud.sb.broker.util.servicecontext.ServiceContextHelper
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper
import groovy.util.logging.Slf4j
import org.springframework.cloud.servicebroker.model.CloudFoundryContext
import org.springframework.data.util.Pair
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicBoolean

@Slf4j
class KubernetesFacadeRedisSpec extends Specification {

    private final static String TEMPLATE_EXAMPLE = """apiVersion: v1
kind: Namespace
metadata:
  name: \"\$SERVICE_ID\"
  labels:
    service_id: \"\$SERVICE_ID\"
    service_type: redis-sentinel
    space: \"\$SPACE_ID\"
    org: \"\$ORG_ID\"
"""

    KubernetesFacadeRedis kubernetesRedisClientRedisDecorated
    KubernetesClient kubernetesClient
    KubernetesConfig kubernetesConfig
    TemplateConfig templateConfig
    EndpointMapperParamsDecorated endpointMapperParamsDecorated
    KubernetesRedisConfig kubernetesRedisConfig
    ProvisionRequest provisionRequest
    DeprovisionRequest deprovisionRequest

    def setup() {
        kubernetesClient = Mock()
        kubernetesConfig = Stub()
        templateConfig = Mock()
        templateConfig.getTemplates(_) >> [TEMPLATE_EXAMPLE, TEMPLATE_EXAMPLE]
        endpointMapperParamsDecorated = Mock()
        deprovisionRequest = Stub()

        kubernetesRedisConfig = Stub()
        kubernetesRedisConfig.templateConfig >> templateConfig
        kubernetesRedisConfig.kubernetesRedisHost >> "host.redis"
        kubernetesRedisConfig.redisConfigurationDefaults >> ["testing": "test"]
        endpointMapperParamsDecorated.getEndpointUrlByTypeWithParams(_, _) >> new Pair("/endpoint/", new NamespaceResponseDto())

        mockProvisionRequest()
        and:
        kubernetesRedisClientRedisDecorated = new KubernetesFacadeRedis(kubernetesClient,
                                                                        kubernetesConfig,
                                                                        endpointMapperParamsDecorated,
                                                                        kubernetesRedisConfig)
    }


    def "provision creating a namespace with correct endpoint called"() {
        when:
        kubernetesRedisClientRedisDecorated.provision(provisionRequest)
        then:
        2 * kubernetesClient.exchange('/endpoint/', HttpMethod.POST, _, NamespaceResponseDto.class)
    }

    def "provision creating a namespace with replacing the organization"() {
        when:
        updateTemplates("""org: \"\$ORG_ID\"
kind: Namespace""")
        and:
        kubernetesRedisClientRedisDecorated.provision(provisionRequest)
        then:
        1 * kubernetesClient.exchange('/endpoint/',
                                      HttpMethod.POST,
                                      "org: \"ORG\"\nkind: Namespace",
                                      NamespaceResponseDto.class)
    }



    def "provision creating a namespace with replacing the space id"() {
        when:
        updateTemplates("space: \"\$SPACE_ID\"\nkind: Namespace")
        and:
        kubernetesRedisClientRedisDecorated.provision(provisionRequest)
        then:
        1 * kubernetesClient.exchange('/endpoint/',
                                      HttpMethod.POST,
                                      "space: \"SPACE\"\nkind: Namespace",
                                      NamespaceResponseDto.class)
    }

    def "provision creating a namespace with replacing the Service Instance Guid id"() {
        when:
        updateTemplates("name: \"\$SERVICE_ID\"\nkind: Namespace")
        and:
        kubernetesRedisClientRedisDecorated.provision(provisionRequest)
        then:
        1 * kubernetesClient.exchange('/endpoint/',
                                      HttpMethod.POST,
                                      "name: \"ID\"\nkind: Namespace",
                                      NamespaceResponseDto.class)
    }

    def "return correct port to the client from k8s"() {
        when:
        updateTemplates("name: \"\$SERVICE_ID\"\nkind: Namespace")
        kubernetesClient.exchange(_, _, _, _) >> new ResponseEntity(mockServiceResponse(), HttpStatus.ACCEPTED)
        and:
        List<ServiceDetail> results = kubernetesRedisClientRedisDecorated.provision(provisionRequest)
        then:
        "112" == ServiceDetailsHelper.from(results).
                getValue(KubernetesRedisServiceDetailKey.KUBERNETES_REDIS_PORT_MASTER)
    }

    def "return correct host to the client from SB"() {
        when:
        updateTemplates("name: \"\$SERVICE_ID\"\nkind: Namespace")
        kubernetesClient.exchange(_, _, _, _) >> new ResponseEntity(mockServiceResponse(), HttpStatus.ACCEPTED)
        and:
        List<ServiceDetail> results = kubernetesRedisClientRedisDecorated.provision(provisionRequest)
        then:
        "host.redis" == ServiceDetailsHelper.from(results).
                getValue(KubernetesRedisServiceDetailKey.KUBERNETES_REDIS_HOST)
    }

    def "returned password has proper length"() {
        when:
        updateTemplates("name: \"\$SERVICE_ID\"\nkind: Namespace")
        kubernetesClient.exchange(_, _, _, _) >> new ResponseEntity(mockServiceResponse(), HttpStatus.ACCEPTED)
        and:
        List<ServiceDetail> results = kubernetesRedisClientRedisDecorated.provision(provisionRequest)
        then:
        30 <= ServiceDetailsHelper.from(results).
                getValue(KubernetesRedisServiceDetailKey.KUBERNETES_REDIS_PASSWORD).
                length()
    }

    def "deletion of service calls proper endpoint"() {
        when:
        kubernetesClient = Mock()
        deprovisionRequest.serviceInstanceGuid >> "GUID"
        and:
        kubernetesRedisClientRedisDecorated.deprovision(deprovisionRequest)
        then:
        1 * kubernetesClient.exchange('/api/v1/namespaces/GUID', HttpMethod.DELETE, "", Object.class)
    }

    def "assert that KubernetesRedisServiceProvider is of CloudFoundryContextRestrictedOnly"() {
        expect:
        def serviceProvider = new KubernetesRedisServiceProvider(Mock(AsyncProvisioningService),
                                                                 Mock(ProvisioningPersistenceService),
                                                                 Mock(KubernetesRedisConfig),
                                                                 Mock(KubernetesFacadeRedis))
        serviceProvider instanceof CloudFoundryContextRestrictedOnly
    }

    def "assert getBindingMap is thread safe"() {
        given:
        AtomicBoolean raceConditionHit = new AtomicBoolean(false)
        Plan plan = new Plan()
        plan.setGuid("test")
        ServiceContext ctx = new ServiceContext()
        ctx.setPlatform("cloudfoundry")
        ctx.setDetails([ServiceContextDetail.of("space_guid", "test"), ServiceContextDetail.of("organization_guid",
                                                                                               "test")].toSet())

        when:
        List threads = new ArrayList()
        for (int i = 0; i < 10; i++) {
            def t = new Thread({
                def pr = new ProvisionRequest()
                pr.setServiceInstanceGuid(UUID.randomUUID().toString())
                pr.setPlan(plan)
                pr.setServiceContext(ctx)
                Map<String, String> bindingMap = kubernetesRedisClientRedisDecorated.getBindingMap(pr)
                if (pr.getServiceInstanceGuid() != bindingMap.get(BaseTemplateConstants.SERVICE_ID.getValue())) {
                    raceConditionHit.set(true)
                    log.info("Race Condition: " + pr.getServiceInstanceGuid() + " != " + bindingMap.get(
                            BaseTemplateConstants.SERVICE_ID.getValue()))
                }
            })
            t.start()
            threads.add(t)
        }
        for (int i = 0; i < threads.size(); i++) {
            ((Thread) threads.get(i)).join()
        }
        then:
        !raceConditionHit.get()
        noExceptionThrown()
    }

    private ServiceResponse mockServiceResponse() {
        ServiceResponse serviceResponse = Stub()
        Spec spec = Stub()
        SelectorDto selector = Stub()
        selector.role >> "master"
        spec.selector >> selector
        mockPorts(spec)
        serviceResponse.spec >> spec
        serviceResponse
    }

    private void mockPorts(Spec spec) {
        Port port = Stub()
        port.name >> "redis-master"
        port.nodePort >> 112
        spec.ports >> [port]
    }

    private void updateTemplates(String templateConfig) {
        this.templateConfig.getTemplates(_) >> [templateConfig]
    }

    private void mockProvisionRequest() {
        def serviceContext = new ServiceContext()
        serviceContext.platform = CloudFoundryContext.CLOUD_FOUNDRY_PLATFORM
        serviceContext.details << ServiceContextDetail.of(ServiceContextHelper.CF_ORGANIZATION_GUID, "ORG")
        serviceContext.details << ServiceContextDetail.of(ServiceContextHelper.CF_SPACE_GUID, "SPACE")

        provisionRequest = Mock(ProvisionRequest)
        provisionRequest.getServiceInstanceGuid() >> "ID"
        provisionRequest.plan >> Mock(Plan)
        provisionRequest.serviceContext >> serviceContext
        provisionRequest.plan.parameters >> new HashSet<Parameter>() {
            {
                add(new Parameter(name: "name", value: "value"))
            }
        }
    }
}