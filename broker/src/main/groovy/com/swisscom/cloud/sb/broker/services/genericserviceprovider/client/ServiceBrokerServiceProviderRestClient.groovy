package com.swisscom.cloud.sb.broker.services.genericserviceprovider.client

import com.swisscom.cloud.sb.broker.config.ApplicationUserConfig
import com.swisscom.cloud.sb.broker.config.UserConfig
import com.swisscom.cloud.sb.broker.services.genericserviceprovider.config.ServiceBrokerServiceProviderConfig
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate

class ServiceBrokerServiceProviderRestClient {

    private UserConfig cfAdminUser
    private UserConfig cfExtUser
    // maybe put url into sbspConfig?
    private ServiceBrokerServiceProviderConfig sbspConfig
    private RestTemplateBuilder restTemplateBuilder

    @Autowired
    EceRestClient(UserConfig cfAdminUser, UserConfig cfExtUser, ServiceBrokerServiceProviderConfig sbspConfig, RestTemplateBuilder restTemplateBuilder) {
        this.cfAdminUser = cfAdminUser
        this.cfExtUser = cfExtUser
        this.sbspConfig = sbspConfig
        this.restTemplateBuilder = restTemplateBuilder
    }

    HttpStatus getServiceInstanceInformation(String serviceInstanceId) {
        def response = createExternalRestTemplate().exchange("http://localhost:8080/v2/cf-ext/${serviceInstanceId}/endpoint", HttpMethod.GET, null, String.class)
        return response.statusCode == HttpStatus.ACCEPTED
    }

    private RestTemplate createAdminRestTemplate() {
        restTemplateBuilder.withBasicAuthentication(cfAdminUser.username, cfAdminUser.password).build()
    }

    private RestTemplate createExternalRestTemplate() {
        restTemplateBuilder.withBasicAuthentication(cfExtUser.username, cfExtUser.password).build()
    }
}
