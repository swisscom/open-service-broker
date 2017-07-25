package com.swisscom.cloud.sb.broker.services.openwhisk

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.BindResponse
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.provisioning.ProvisionResponse
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import com.swisscom.cloud.sb.broker.util.RestTemplateFactory
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.util.ServiceDetailKey
import org.apache.commons.lang.RandomStringUtils
import org.springframework.beans.factory.annotation.Autowired
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component

@Component
@CompileStatic
@Slf4j
class OpenWhiskServiceProvider implements ServiceProvider{

    private final OpenWhiskConfig owConfig

    private final RestTemplateFactory restTemplateFactory

    private final OpenWhiskDbClient owDbClient

    private JsonNode docs

    @Autowired
    private final ObjectMapper mapper

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository

    @Autowired
    private ServiceBindingRepository serviceBindingRepository

    @Autowired
    OpenWhiskServiceProvider(OpenWhiskConfig owConfig, RestTemplateFactory restTemplateFactory, OpenWhiskDbClient owDbClient) {
        this.owConfig = owConfig
        this.restTemplateFactory = restTemplateFactory
        this.owDbClient = owDbClient
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
        docs = subjectHelper(namespace, subject, uuid, key)
        String res = owDbClient.insertIntoDatabase(docs)
        log.info("Namespace created.")

        String url = "${owConfig.openWhiskProtocol}://${owConfig.openWhiskHost}${owConfig.openWhiskPath}web/${namespace}/"

        return new ProvisionResponse(details: [ServiceDetail.from(ServiceDetailKey.OPENWHISK_UUID, uuid),
                                               ServiceDetail.from(ServiceDetailKey.OPENWHISK_KEY, key),
                                               ServiceDetail.from(ServiceDetailKey.OPENWHISK_URL, url),
                                               ServiceDetail.from(ServiceDetailKey.OPENWHISK_NAMESPACE, namespace)], isAsync: false)
    }

    @Override
    DeprovisionResponse deprovision(DeprovisionRequest request){

        String namespace = getNamespace(request.serviceInstanceGuid)

        String doc = owDbClient.getSubjectFromDB(namespace)
        if (doc == null){
            log.error("Subject not found.")
            ErrorCode.OPENWHISK_SUBJECT_NOT_FOUND.throwNew()
        }

        docs = mapper.readTree(doc)

        Integer ns_index = null
        ArrayNode namespaceArray = (ArrayNode) docs.path("namespaces")
        namespaceArray.each {
            if (it.path("name").asText() == namespace) {
                ns_index = it.intValue()
            }
        }

        if (ns_index != null) {
            namespaceArray.remove(ns_index)
        } else {
            log.error("Namespace does not exist - Returning 410")
            ErrorCode.OPENWHISK_NAMESPACE_ALREADY_EXISTS.throwNew()
        }

        String res = owDbClient.insertIntoDatabase(docs)
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
        docs = subjectHelper(namespace, subject, uuid, key)
        String res = owDbClient.insertIntoDatabase(docs)
        log.info("Subject created.")

        String url = "${owConfig.openWhiskProtocol}://${owConfig.openWhiskHost}${owConfig.openWhiskPath}web/${namespace}/"

        return new BindResponse(details: [ServiceDetail.from(ServiceDetailKey.OPENWHISK_UUID, uuid),
                                          ServiceDetail.from(ServiceDetailKey.OPENWHISK_KEY, key),
                                          ServiceDetail.from(ServiceDetailKey.OPENWHISK_URL, url),
                                          ServiceDetail.from(ServiceDetailKey.OPENWHISK_SUBJECT, subject),
                                          ServiceDetail.from(ServiceDetailKey.OPENWHISK_NAMESPACE, namespace)],
                credentials: new OpenWhiskBindResponseDto(openwhiskUrl: url, openwhiskUUID: uuid, openwhiskKey: key,
                                                          openwhiskNamespace: namespace, openwhiskSubject: subject))
    }

    @Override
    void unbind(UnbindRequest request){

        String subject = getSubject(request.binding.guid)
        String doc = owDbClient.getSubjectFromDB(subject)
        if (doc == null){
            log.error("Subject not found.")
            ErrorCode.OPENWHISK_SUBJECT_NOT_FOUND.throwNew()
        }

        docs = mapper.readTree(doc)
        String rev = docs.path("_rev").asText()
        String res = owDbClient.deleteSubjectFromDb(subject, rev)
        log.info("Subject deleted.")

    }

    JsonNode subjectHelper(String namespace, String subject, String uuid, String key) {
        String doc = owDbClient.getSubjectFromDB(subject)
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

    String getNamespace(String service_id) {
        ServiceInstance si = serviceInstanceRepository.findByGuid(service_id)
        String namespace = null
        si.details.each {
            if (it.key == "openwhisk_namespace") {
                namespace = it.value
            }
        }

        return namespace
    }

    String getSubject(String bind_id) {
        ServiceBinding sb = serviceBindingRepository.findByGuid(bind_id)
        String subject = null
        sb.details.each {
            if (it.key == "openwhisk_subject") {
                subject = it.value
            }
        }

        return subject
    }
}
