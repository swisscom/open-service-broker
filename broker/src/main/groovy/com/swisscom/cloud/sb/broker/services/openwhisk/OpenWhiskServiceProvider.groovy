package com.swisscom.cloud.sb.broker.services.openwhisk

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.google.common.annotations.VisibleForTesting
import com.google.common.base.Optional
import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.BindResponse
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.cfextensions.serviceusage.ServiceUsageProvider
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.provisioning.ProvisionResponse
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import com.swisscom.cloud.sb.broker.util.RestTemplateFactory
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.util.ServiceDetailKey
import com.swisscom.cloud.sb.broker.util.ServiceDetailsHelper
import com.swisscom.cloud.sb.model.usage.ServiceUsage
import com.swisscom.cloud.sb.model.usage.ServiceUsageType
import com.swisscom.cloud.sb.model.usage.ServiceUsageUnit
import org.apache.commons.lang.RandomStringUtils
import org.springframework.beans.factory.annotation.Autowired
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component

import static com.swisscom.cloud.sb.broker.model.ServiceDetail.from
import static com.swisscom.cloud.sb.broker.util.ServiceDetailKey.OPENWHISK_ADMIN_URL
import static com.swisscom.cloud.sb.broker.util.ServiceDetailKey.OPENWHISK_EXECUTION_URL
import static com.swisscom.cloud.sb.broker.util.ServiceDetailKey.OPENWHISK_KEY
import static com.swisscom.cloud.sb.broker.util.ServiceDetailKey.OPENWHISK_NAMESPACE
import static com.swisscom.cloud.sb.broker.util.ServiceDetailKey.OPENWHISK_SUBJECT
import static com.swisscom.cloud.sb.broker.util.ServiceDetailKey.OPENWHISK_UUID

@Component
@CompileStatic
@Slf4j
class OpenWhiskServiceProvider implements ServiceProvider, ServiceUsageProvider{

    private final OpenWhiskConfig openWhiskConfig
    private final RestTemplateFactory restTemplateFactory
    private final OpenWhiskDbClient openWhiskDbClient
    private final ServiceInstanceRepository serviceInstanceRepository
    private final ServiceBindingRepository serviceBindingRepository

    @Autowired
    OpenWhiskServiceProvider(OpenWhiskConfig openWhiskConfig, RestTemplateFactory restTemplateFactory, OpenWhiskDbClient openWhiskDbClient, ServiceInstanceRepository serviceInstanceRepository, ServiceBindingRepository serviceBindingRepository) {
        this.openWhiskConfig = openWhiskConfig
        this.restTemplateFactory = restTemplateFactory
        this.openWhiskDbClient = openWhiskDbClient
        this.serviceInstanceRepository = serviceInstanceRepository
        this.serviceBindingRepository = serviceBindingRepository
    }

    @Override
    ProvisionResponse provision(ProvisionRequest request){
        ObjectMapper mapper = new ObjectMapper()
        JsonNode params =  null
        if (request.parameters != null) {
            params = mapper.readTree(request.parameters)
        }

        def namespace = validateNamespace(params)
        def subject = namespace

        def uuid = UUID.randomUUID().toString()
        def key = RandomStringUtils.randomAlphanumeric(64)
        openWhiskDbClient.insertIntoDatabase(subjectHelper(namespace, subject, uuid, key))
        log.info("Namespace created.")

        return new ProvisionResponse(details: [from(OPENWHISK_UUID, uuid),
                                               from(OPENWHISK_KEY, key),
                                               from(OPENWHISK_EXECUTION_URL, "${openWhiskConfig.openWhiskProtocol}://${openWhiskConfig.openWhiskHost}${openWhiskConfig.openWhiskPath}web/${namespace}"),
                                               from(OPENWHISK_ADMIN_URL, "${openWhiskConfig.openWhiskProtocol}://${openWhiskConfig.openWhiskHost}${openWhiskConfig.openWhiskPath}namespaces"),
                                               from(OPENWHISK_NAMESPACE, namespace)], isAsync: false)
    }

    @Override
    DeprovisionResponse deprovision(DeprovisionRequest request){
        deleteEntity(getNamespace(request.serviceInstanceGuid))
        log.info("Namespace deleted.")

        return new DeprovisionResponse(isAsync: false)
    }

    @Override
    BindResponse bind(BindRequest request){
        String namespace = getNamespace(request.serviceInstance.guid)

        def subject = validateSubject(request.parameters)

        def uuid = UUID.randomUUID().toString()
        def key = RandomStringUtils.randomAlphanumeric(64)
        openWhiskDbClient.insertIntoDatabase(subjectHelper(namespace, subject, uuid, key))
        log.info("Subject created.")

        String url = "${openWhiskConfig.openWhiskProtocol}://${openWhiskConfig.openWhiskHost}${openWhiskConfig.openWhiskPath}web/${namespace}"
        String adminUrl = "${openWhiskConfig.openWhiskProtocol}://${openWhiskConfig.openWhiskHost}${openWhiskConfig.openWhiskPath}namespaces"

        return new BindResponse(details: [from(OPENWHISK_UUID, uuid),
                                          from(OPENWHISK_KEY, key),
                                          from(OPENWHISK_EXECUTION_URL, url),
                                          from(OPENWHISK_ADMIN_URL, adminUrl),
                                          from(OPENWHISK_SUBJECT, subject),
                                          from(OPENWHISK_NAMESPACE, namespace)],
                credentials: new OpenWhiskBindResponseDto(openwhiskExecutionUrl: url, openwhiskAdminUrl: adminUrl, openwhiskUUID: uuid, openwhiskKey: key,
                                                          openwhiskNamespace: namespace, openwhiskSubject: subject))
    }

    @Override
    void unbind(UnbindRequest request){
        deleteEntity(getSubject(request.binding.guid))
        log.info("Subject deleted.")
    }

    @Override
    ServiceUsage findUsage(ServiceInstance serviceInstance, Optional<Date> enddate) {
        ObjectMapper mapper = new ObjectMapper()
        ArrayNode usageArray = (ArrayNode) mapper.readTree(openWhiskDbClient.getUsageForNamespace(ServiceDetailsHelper.from(serviceInstance).getValue(ServiceDetailKey.OPENWHISK_NAMESPACE))).path("rows")
        def usageMS = 0
        usageArray.each {
            usageMS = usageMS + it.path("value").intValue()
        }
        return new ServiceUsage(value: (usageMS/1000).toString(), type: ServiceUsageType.TRANSACTIONS, unit: ServiceUsageUnit.MEGABYTE_SECOND)
    }

    @VisibleForTesting
    private JsonNode subjectHelper(String namespace, String subject, String uuid, String key) {
        String doc = openWhiskDbClient.getSubjectFromDB(subject)
        JsonNode docs
        ObjectMapper mapper = new ObjectMapper()
        if (doc == null) {
            doc = """{"_id": "${subject}",
                        "subject": "${subject}",
                        "namespaces": [
                            {
                                "name": "${namespace}",
                                "uuid": "${uuid}",
                                "key": "${key}"
                            }
                        ]
                    }"""
            docs = mapper.readTree(doc)
        } else {
            docs = mapper.readTree(doc)
            ArrayNode namespaceArray = (ArrayNode) docs.path("namespaces")
            namespaceArray.each {
                if (it.path("name").asText() == namespace) {
                    log.error("Namespace or subject already exists - Returning 410")
                    ErrorCode.OPENWHISK_NAMESPACE_ALREADY_EXISTS.throwNew()
                }
            }
            namespaceArray.add(mapper.readTree("""{"name": "${namespace}", "uuid": "${uuid}", "key": "${key}"}"""))
            docs["namespaces"] = namespaceArray
        }

        return docs
    }

    @VisibleForTesting
    private String validateSubject(Map params) {
        if (params == null) {
            return UUID.randomUUID().toString()
        } else if (params.containsKey("subject")) {
            String subject = params["subject"]
            if (subject.length() < 5) {
                ErrorCode.OPENWHISK_CANNOT_CREATE_NAMESPACE.throwNew("- Subject name must be at least 5 characters")
            }
            return subject
        } else {
            return UUID.randomUUID().toString()
        }
    }

    @VisibleForTesting
    private String validateNamespace(JsonNode params){
        if (params == null) {
            return UUID.randomUUID().toString()
        } else if (!params.has("namespace")) {
            return UUID.randomUUID().toString()
        } else {
            def namespace = params.path("namespace").asText().trim()
            if (namespace.length() < 5) {
                ErrorCode.OPENWHISK_CANNOT_CREATE_NAMESPACE.throwNew("- Namespace name must be at least 5 characters")
            }
            return namespace
        }
    }

    @VisibleForTesting
    private String getNamespace(String serviceId) {
        return ServiceDetailsHelper.from(serviceInstanceRepository.findByGuid(serviceId).details).getValue(OPENWHISK_NAMESPACE)
    }

    @VisibleForTesting
    private String getSubject(String bindId) {
        return ServiceDetailsHelper.from(serviceBindingRepository.findByGuid(bindId).details).getValue(OPENWHISK_SUBJECT)
    }

    @VisibleForTesting
    private void deleteEntity(String entity) {
        String doc = openWhiskDbClient.getSubjectFromDB(entity)
        if (doc == null){
            log.error("Subject not found.")
            ErrorCode.OPENWHISK_SUBJECT_NOT_FOUND.throwNew()
        }
        ObjectMapper mapper = new ObjectMapper()

        openWhiskDbClient.deleteSubjectFromDb(entity, mapper.readTree(doc).path("_rev").asText())
    }
}
