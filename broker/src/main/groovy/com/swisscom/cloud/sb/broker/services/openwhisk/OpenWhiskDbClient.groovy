package com.swisscom.cloud.sb.broker.services.openwhisk

import com.fasterxml.jackson.databind.JsonNode
import com.swisscom.cloud.sb.broker.util.RestTemplateFactory
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@Component
@CompileStatic
@Slf4j
class OpenWhiskDbClient {

    public final String protocol
    public final String host
    public final String port
    public final String username
    public final String password
    public final String localUser
    public final String hostname

    private final RestTemplate restTemplate

    @Autowired
    OpenWhiskDbClient(OpenWhiskConfig openWhiskConfig, RestTemplateFactory restTemplateFactory){
        this.protocol = openWhiskConfig.openWhiskDbProtocol
        this.host = openWhiskConfig.openWhiskDbHost
        this.port = openWhiskConfig.openWhiskDbPort
        this.username = openWhiskConfig.openWhiskDbUser
        this.password = openWhiskConfig.openWhiskDbPass
        this.localUser = openWhiskConfig.openWhiskDbLocalUser
        this.hostname = openWhiskConfig.openWhiskDbHostname

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

        return res.getBody()
    }

    String insertIntoDatabase(JsonNode payload){

        def url = "${protocol}://${host}:${port}/${localUser}_${hostname}_subjects"

        ResponseEntity<String> res = restTemplate.postForEntity(url, payload, String.class)

        return res.getBody()
    }

    String deleteSubjectFromDb(String subject, String rev) {

        def url = "${protocol}://${host}:${port}/${localUser}_${hostname}_subjects/${subject}?rev=${rev}"

        ResponseEntity<String> res =  restTemplate.exchange(url, HttpMethod.DELETE,null, String.class)

        return res.getBody()
    }
}
