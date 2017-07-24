package com.swisscom.cloud.sb.broker.services.openwhisk

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.util.RestTemplateFactory
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@Component
@CompileStatic
@Slf4j
class OpenWhiskDbClient {

    private final String protocol
    private final String host
    private final String port
    private final String username
    private final String password
    private final String localUser
    private final String hostname

    private final RestTemplate restTemplate

    @Autowired
    private final ObjectMapper mapper

    @Autowired
    OpenWhiskDbClient(OpenWhiskConfig owConfig, RestTemplateFactory restTemplateFactory){
        this.protocol = owConfig.openWhiskDbProtocol
        this.host = owConfig.openWhiskDbHost
        this.port = owConfig.openWhiskDbPort
        this.username = owConfig.openWhiskDbUser
        this.password = owConfig.openWhiskDbPass
        this.localUser = owConfig.openWhiskDbLocalUser
        this.hostname = owConfig.openWhiskDbHostname

        this.restTemplate = restTemplateFactory.buildWithBasicAuthentication(username, password)

    }

    String getSubjectFromDB(String subject){

        def url = "${protocol}://${host}:${port}/${localUser}_${hostname}_subjects/${subject}"

        ResponseEntity<String> res

        try {
            res = restTemplate.getForEntity(url, String.class)
        } catch (HttpClientErrorException ex) {
            log.info("Http error exception = ${ex}")
            log.info("Subject does not exist")
            return null
        }

        if (res.getStatusCodeValue() == 200){
            return res.getBody()
        } else {
            log.info("OpenWhisk database returned ${res.getStatusCodeValue()}")
            return null
        }
    }

    String insertIntoDatabase(JsonNode payload){

        def url = "${protocol}://${host}:${port}/${localUser}_${hostname}_subjects"

        ResponseEntity<String> res = restTemplate.postForEntity(url, payload, String.class)

        if (res.getStatusCodeValue() == 200 || res.getStatusCodeValue() == 201){
            return res.getBody()
        } else {
            log.error("Failed to insert into database. Status code - ${res.getStatusCodeValue()}")
            ErrorCode.OPENWHISK_CANNOT_CREATE_NAMESPACE.throwNew()
        }
    }

}
