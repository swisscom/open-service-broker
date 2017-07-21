package com.swisscom.cloud.sb.broker.services.openwhisk

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.BindResponse
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceDetail
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
    OpenWhiskServiceProvider(OpenWhiskConfig owConfig, RestTemplateFactory restTemplateFactory, OpenWhiskDbClient owDbClient) {
        this.owConfig = owConfig
        this.restTemplateFactory = restTemplateFactory
        this.owDbClient = owDbClient
    }

//    @PostConstruct
//    def init() {
//        log.info("owConfig = ${owConfig}")
//        RestTemplate rt = restTemplateFactory.buildWithSSLValidationDisabledAndBasicAuthentication(owConfig.openWhiskAdminKey, owConfig.openWhiskAdminPass)
//
//        ResponseEntity<String> re = rt.getForEntity(owConfig.openWhiskUrl, String.class)
//
//        log.info("RestTemplate rt = ${rt}")
//        log.info("ResponseEntity re = ${re}")
//        log.info("getBody = ${re.getBody()}")
//
////        ObjectMapper mapper = new ObjectMapper()
////        JsonNode root = mapper.readTree(re.getBody())
//
//    }

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

        String doc = owDbClient.getSubjectFromDB(subject)
        def uuid = UUID.randomUUID().toString()
        def key = RandomStringUtils.randomAlphanumeric(64)
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
                    log.error("Namespace already exists - Returning 410")
                    ErrorCode.OPENWHISK_NAMESPACE_ALREADY_EXISTS.throwNew()
                }
            }
            String newNode = "{\"name\": \"${namespace}\", \"uuid\": \"${uuid}\", \"key\": \"${key}\"}"
            JsonNode nsNode = mapper.readTree(newNode)
            namespaceArray.add(nsNode)
            docs.putAt("namespaces", namespaceArray)
        }

        String res = owDbClient.insertIntoDatabase(docs)

        String url = "${owConfig.openWhiskProtocol}://${owConfig.openWhiskHost}${owConfig.openWhiskPath}web/${namespace}/"

        return new ProvisionResponse(details: [ServiceDetail.from(ServiceDetailKey.OPENWHISK_UUID, uuid),
                                               ServiceDetail.from(ServiceDetailKey.OPENWHISK_KEY, key),
                                               ServiceDetail.from(ServiceDetailKey.OPENWHISK_URL, url),
                                               ServiceDetail.from(ServiceDetailKey.OPENWHISK_NAMESPACE, namespace)], isAsync: false)
    }

    @Override
    DeprovisionResponse deprovision(DeprovisionRequest request){
        println "In deprovision OW"
        println "DeprovisionRequest - request"
        println request.toString()
//        JsonNode params = mapper.readTree(request)
//
//        def subject = params.path("subject").asText().trim()
//        def namespace = params.path("namespace").asText().trim()
//
//        if (!params.has("namespace")) {
//            namespace = subject
//        }
//
//        if (subject.length() < 5) {
//            log.error("Subject name must be at least 5 characters")
//            ErrorCode.OPENWHISK_CANNOT_CREATE_NAMESPACE.throwNew("- Subject name must be at least 5 characters")
//        }
//
//        if (namespace == '') {
//            log.error("Namespace cannot be empty string")
//            ErrorCode.OPENWHISK_CANNOT_CREATE_NAMESPACE.throwNew("- Namespace cannot be empty string")
//        }
        return new DeprovisionResponse(isAsync: false)
    }

    @Override
    BindResponse bind(BindRequest request){
        println "IN bind"
        println "BindRequest - request"
        println "app_guid " + request.app_guid
        return new BindResponse(details: [ServiceDetail.from("username", "pass")],
                credentials: new OpenWhiskBindResponseDto(openwhiskAdminKey: "username1", openwhiskAdminPass: "password1"))
    }

    @Override
    void unbind(UnbindRequest request){
        println "In unbind"
    }
}
