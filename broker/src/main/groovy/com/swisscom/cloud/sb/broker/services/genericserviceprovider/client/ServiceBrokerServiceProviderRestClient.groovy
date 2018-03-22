package com.swisscom.cloud.sb.broker.services.genericserviceprovider.client

import com.swisscom.cloud.sb.broker.config.ApplicationUserConfig
import com.swisscom.cloud.sb.broker.config.UserConfig
import com.swisscom.cloud.sb.broker.services.genericserviceprovider.config.ServiceBrokerServiceProviderConfig
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
@CompileStatic
@Slf4j
class ServiceBrokerServiceProviderRestClient {

    // maybe put url into sbspConfig?
    private ServiceBrokerServiceProviderConfig sbspConfig
    private RestTemplateBuilder restTemplateBuilder

    @Autowired
    EceRestClient(ServiceBrokerServiceProviderConfig sbspConfig, RestTemplateBuilder restTemplateBuilder) {
        this.sbspConfig = sbspConfig
        this.restTemplateBuilder = restTemplateBuilder
    }

    boolean getServiceInstanceInformation(String serviceInstanceId) {
        def response = createExternalRestTemplate().exchange("http://localhost:8080/v2/cf-ext/${serviceInstanceId}/endpoint", HttpMethod.GET, null, String.class)
        return response.statusCode == HttpStatus.ACCEPTED
    }

    // Also returns true if serviceInstance never existed in the first place
    /*boolean getServiceInstanceDoesNotExist(String serviceInstanceId) {
        def response = createExternalRestTemplate().exchange("http://localhost:8080/v2/cf-ext/${serviceInstanceId}/endpoint")
        return response.statusCode == HttpStatus.GONE
    }*/

    /*private RestTemplate createAdminRestTemplate() {
        restTemplateBuilder.withBasicAuthentication(cfAdminUser.username, cfAdminUser.password).build()
    }*/

    private RestTemplate createExternalRestTemplate() {
        restTemplateBuilder.withBasicAuthentication("cc_ext", "change_me").build()
    }
}
