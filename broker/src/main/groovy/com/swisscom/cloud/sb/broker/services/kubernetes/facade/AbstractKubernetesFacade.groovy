package com.swisscom.cloud.sb.broker.services.kubernetes.facade

import com.swisscom.cloud.sb.broker.util.servicecontext.ServiceContextHelper
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.services.kubernetes.client.rest.KubernetesClient
import com.swisscom.cloud.sb.broker.services.kubernetes.config.AbstractKubernetesServiceConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.config.KubernetesConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.endpoint.EndpointMapper
import com.swisscom.cloud.sb.broker.services.kubernetes.endpoint.parameters.EndpointMapperParamsDecorated
import com.swisscom.cloud.sb.broker.services.kubernetes.endpoint.parameters.KubernetesConfigUrlParams
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplate
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplateManager
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.constants.BaseTemplateConstants
import groovy.json.JsonSlurper
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.CloudFoundryContext
import org.springframework.data.util.Pair
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpStatusCodeException

import static groovy.transform.TypeCheckingMode.SKIP

@Slf4j
abstract class AbstractKubernetesFacade<T extends AbstractKubernetesServiceConfig> implements KubernetesFacade {
    protected final KubernetesClient<?> kubernetesClient
    protected final KubernetesConfig kubernetesConfig
    protected final KubernetesTemplateManager kubernetesTemplateManager
    protected final EndpointMapperParamsDecorated endpointMapperParamsDecorated
    protected final T kubernetesServiceConfig

    @Autowired
    AbstractKubernetesFacade(KubernetesClient kubernetesClient, KubernetesConfig kubernetesConfig, KubernetesTemplateManager kubernetesTemplateManager, EndpointMapperParamsDecorated endpointMapperParamsDecorated, AbstractKubernetesServiceConfig abstractKubernetesServiceConfig) {
        this.kubernetesClient = kubernetesClient
        this.kubernetesConfig = kubernetesConfig
        this.kubernetesTemplateManager = kubernetesTemplateManager
        this.endpointMapperParamsDecorated = endpointMapperParamsDecorated
        this.kubernetesServiceConfig = abstractKubernetesServiceConfig
    }

    protected abstract Map<String, String> getBindingMap(ProvisionRequest context)

    protected
    abstract Collection<ServiceDetail> buildServiceDetailsList(Map<String, String> bindingMap, List<ResponseEntity> responses)

    Collection<ServiceDetail> provision(ProvisionRequest context) {
        def bindingMap = getBindingMap(context)
        log.debug("Use this bindings for k8s templates: ${groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(bindingMap))}")
        def templates = kubernetesTemplateManager.getTemplates(context.plan.templateUniqueIdentifier)
        def templateEngine = new groovy.text.SimpleTemplateEngine()
        List<ResponseEntity> responses = new LinkedList()
        for (KubernetesTemplate kubernetesTemplate : templates) {
            def bindedTemplate = templateEngine.createTemplate(fixTemplateEscaping(kubernetesTemplate)).make(bindingMap).toString()
            log.trace("Request this template for k8s provision: ${bindedTemplate}")
            Pair<String, ?> urlReturn = endpointMapperParamsDecorated.getEndpointUrlByTypeWithParams(KubernetesTemplate.getKindForTemplate(bindedTemplate), (new KubernetesConfigUrlParams()).getParameters(context))
            responses.add(kubernetesClient.exchange(urlReturn.getFirst(), HttpMethod.POST, bindedTemplate, urlReturn.getSecond().class))
        }
        return buildServiceDetailsList(bindingMap, responses)
    }

    protected Map<String, String> getServiceDetailBindingMap(ProvisionRequest request) {
        def context = ServiceContextHelper.convertFrom(request.serviceContext) as CloudFoundryContext
        [
                (BaseTemplateConstants.SERVICE_ID.getValue()): request.getServiceInstanceGuid(),
                (BaseTemplateConstants.SPACE_ID.getValue())  : context.spaceGuid,
                (BaseTemplateConstants.ORG_ID.getValue())    : context.organizationGuid,
                (BaseTemplateConstants.PLAN_ID.getValue())   : request.plan.guid,
        ]
    }

    protected Map<String, String> getPlanParameterBindingMap(Plan plan) {
        plan.parameters.collectEntries {
            [(it.getName() as String): it.getValue() as String]
        }
    }

    void deprovision(DeprovisionRequest request) {
        try {
            kubernetesClient.exchange(EndpointMapper.INSTANCE.getEndpointUrlByType("Namespace").getFirst() + "/" + request.serviceInstanceGuid,
                    HttpMethod.DELETE, "", Object.class)
        } catch (HttpStatusCodeException e) {
            if (e.statusCode != HttpStatus.NOT_FOUND) throw e
            else {
                log.debug("404: Tried to delete namespace " + request.serviceInstanceGuid + " which was not found. Error Message:" + e.toString())
            }
        }
    }

    @Override
    boolean isKubernetesDeploymentSuccessful(String serviceInstanceGuid) {
        try {
            def pods = getPodList(serviceInstanceGuid)
            def consideredPods = getPodsConsideredForReadiness(pods)
            return checkPodsReadinessState(consideredPods)
        } catch (HttpStatusCodeException e) {
            log.error("Readiness check for kubernetes service with instance guid " + serviceInstanceGuid
                    + " failed, got HTTP status code: " + e.getStatusCode().toString())
            return false
        }
    }

    @Override
    boolean isKubernetesNamespaceDeleted(String serviceInstanceGuid) {
        try {
            def statusCode = getNamespaceStatusCode(serviceInstanceGuid)
            return (statusCode == HttpStatus.NOT_FOUND)
        } catch (HttpStatusCodeException e) {
            if (e.statusCode != HttpStatus.NOT_FOUND) {
                throw e
            } else {
                return true
            }
        }
    }

    private HttpStatus getNamespaceStatusCode(String serviceInstanceGuid) {
        ResponseEntity namespaceResponse = kubernetesClient.exchange(
                EndpointMapper.INSTANCE.getEndpointUrlByType('Namespace').getFirst() + '/' + serviceInstanceGuid,
                HttpMethod.GET, "", String.class)
        return namespaceResponse.statusCode
    }

    @TypeChecked(SKIP)
    private List getPodList(String serviceInstanceGuid) throws HttpStatusCodeException {
        ResponseEntity<String> podListResponse = kubernetesClient.exchange(
                EndpointMapper.INSTANCE.getEndpointUrlByType('Namespace').getFirst() + '/' + serviceInstanceGuid + '/pods',
                HttpMethod.GET, "", String.class)
        def jsonSlurper = new JsonSlurper()
        def podList = jsonSlurper.parseText(podListResponse.body)
        assert podList.kind == 'PodList'
        return podList.items
    }

    /**
     * healthz is an internally defined label in our kubernetes service deployments.
     */
    @TypeChecked(SKIP)
    private List getPodsConsideredForReadiness(List pods) {
        if (kubernetesServiceConfig.enablePodLabelHealthzFilter) {
            return pods.findAll { it.metadata.labels.healthz == "true" }
        } else {
            return pods
        }
    }

    @TypeChecked(SKIP)
    private boolean checkPodsReadinessState(List pods) {
        def podsConditions = pods.collect { it.status.conditions }.flatten()
        def hasConditionsReadyAndStatusFalse = podsConditions.findAll { it.type == 'Ready' }.findAll {
            it.status == 'False'
        }
        return (hasConditionsReadyAndStatusFalse.size() == 0 && !pods.empty)
    }

    /**
     * If a k8s templates contains bash scripts some literals must be escaped additionally because otherwise
     * `SimpleTemplateEngine` will strugle because everything with `$` would be a template expression.
     */
    protected String fixTemplateEscaping(KubernetesTemplate kubernetesTemplate) {
        def escapedTemplate = kubernetesTemplate.template.replace('$(', '\\$(').
                replace('$"', '\\$"')
        escapedTemplate
    }
}