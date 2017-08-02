package com.swisscom.cloud.sb.broker.services.openwhisk

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.google.common.annotations.VisibleForTesting
import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.BindResponse
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.provisioning.ProvisionResponse
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import com.swisscom.cloud.sb.broker.util.RestTemplateFactory
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.util.ServiceDetailKey
import com.swisscom.cloud.sb.broker.util.ServiceDetailsHelper
import org.apache.commons.lang.RandomStringUtils
import org.springframework.beans.factory.annotation.Autowired
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component

import static com.swisscom.cloud.sb.broker.util.ServiceDetailKey.OPENWHISK_NAMESPACE
import static com.swisscom.cloud.sb.broker.util.ServiceDetailKey.OPENWHISK_SUBJECT

@Component
@CompileStatic
@Slf4j
class OpenWhiskServiceProvider implements ServiceProvider{

    private final OpenWhiskConfig openWhiskConfig

    private final RestTemplateFactory restTemplateFactory

    private final OpenWhiskDbClient openWhiskDbClient

    private final ObjectMapper mapper = new ObjectMapper()

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
        JsonNode params = mapper.readTree(request.parameters)

        def namespace = params.path("namespace").asText().trim()

        if (!params.has("namespace")) {
            log.error("Namespace parameter is missing in provision request.")
            ErrorCode.OPENWHISK_CANNOT_CREATE_NAMESPACE.throwNew("- Namespace parameter is missing in provision request.")
        }

        def subject = namespace

        if (subject.length() < 5) {
            log.error("Namespace name must be at least 5 characters")
            ErrorCode.OPENWHISK_CANNOT_CREATE_NAMESPACE.throwNew("- Namespace name must be at least 5 characters")
        }

        def uuid = UUID.randomUUID().toString()
        def key = RandomStringUtils.randomAlphanumeric(64)
        JsonNode docs = subjectHelper(namespace, subject, uuid, key)
        String res = openWhiskDbClient.insertIntoDatabase(docs)
        log.info("Namespace created.")

        String url = "${openWhiskConfig.openWhiskProtocol}://${openWhiskConfig.openWhiskHost}${openWhiskConfig.openWhiskPath}web/${namespace}"
        String adminUrl = "${openWhiskConfig.openWhiskProtocol}://${openWhiskConfig.openWhiskHost}${openWhiskConfig.openWhiskPath}namespaces"

        return new ProvisionResponse(details: [ServiceDetail.from(ServiceDetailKey.OPENWHISK_UUID, uuid),
                                               ServiceDetail.from(ServiceDetailKey.OPENWHISK_KEY, key),
                                               ServiceDetail.from(ServiceDetailKey.OPENWHISK_EXECUTION_URL, url),
                                               ServiceDetail.from(ServiceDetailKey.OPENWHISK_ADMIN_URL, adminUrl),
                                               ServiceDetail.from(OPENWHISK_NAMESPACE, namespace)], isAsync: false)
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
        String subject = request.parameters.getAt("subject")

        if (subject.length() < 5) {
            log.error("Subject name must be at least 5 characters")
            ErrorCode.OPENWHISK_CANNOT_CREATE_NAMESPACE.throwNew("- Subject name must be at least 5 characters")
        }

        def uuid = UUID.randomUUID().toString()
        def key = RandomStringUtils.randomAlphanumeric(64)
        JsonNode docs = subjectHelper(namespace, subject, uuid, key)
        String res = openWhiskDbClient.insertIntoDatabase(docs)
        log.info("Subject created.")

        String url = "${openWhiskConfig.openWhiskProtocol}://${openWhiskConfig.openWhiskHost}${openWhiskConfig.openWhiskPath}web/${namespace}"
        String adminUrl = "${openWhiskConfig.openWhiskProtocol}://${openWhiskConfig.openWhiskHost}${openWhiskConfig.openWhiskPath}namespaces"

        return new BindResponse(details: [ServiceDetail.from(ServiceDetailKey.OPENWHISK_UUID, uuid),
                                          ServiceDetail.from(ServiceDetailKey.OPENWHISK_KEY, key),
                                          ServiceDetail.from(ServiceDetailKey.OPENWHISK_EXECUTION_URL, url),
                                          ServiceDetail.from(ServiceDetailKey.OPENWHISK_ADMIN_URL, adminUrl),
                                          ServiceDetail.from(OPENWHISK_SUBJECT, subject),
                                          ServiceDetail.from(OPENWHISK_NAMESPACE, namespace)],
                credentials: new OpenWhiskBindResponseDto(openwhiskExecutionUrl: url, openwhiskAdminUrl: adminUrl, openwhiskUUID: uuid, openwhiskKey: key,
                                                          openwhiskNamespace: namespace, openwhiskSubject: subject))
    }

    @Override
    void unbind(UnbindRequest request){
        deleteEntity(getSubject(request.binding.guid))
        log.info("Subject deleted.")
    }

    @VisibleForTesting
    private JsonNode subjectHelper(String namespace, String subject, String uuid, String key) {
        String doc = openWhiskDbClient.getSubjectFromDB(subject)
        JsonNode docs
        if (doc == null) {
            doc = "{\"_id\": \"${subject}\"," +
                        "\"subject\": \"${subject}\"," +
                        "\"namespaces\": [" +
                            "{" +
                                "\"name\": \"${namespace}\"," +
                                "\"uuid\": \"${uuid}\"," +
                                "\"key\": \"${key}\"" +
                            "}" +
                        "]" +
                    "}"
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
            String newNode = "{\"name\": \"${namespace}\", \"uuid\": \"${uuid}\", \"key\": \"${key}\"}"
            JsonNode nsNode = mapper.readTree(newNode)
            namespaceArray.add(nsNode)
            docs.putAt("namespaces", namespaceArray)
        }

        return docs
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

        JsonNode docs = mapper.readTree(doc)
        String rev = docs.path("_rev").asText()
        String res = openWhiskDbClient.deleteSubjectFromDb(entity, rev)
    }
}
