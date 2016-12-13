package com.swisscom.cf.broker.functional

import com.swisscom.cf.broker.config.AuthenticationConfig
import com.swisscom.cf.broker.util.ServiceLifeCycler
import com.swisscom.cf.servicebroker.client.ServiceBrokerClient
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.context.WebApplicationContext
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import javax.annotation.PostConstruct

@CompileStatic
@Stepwise
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration
abstract class BaseFunctionalSpec extends Specification {
    @Autowired
    protected WebApplicationContext applicationContext

    @Shared
    protected ServiceLifeCycler serviceLifeCycler
    @Shared
    protected ServiceBrokerClient serviceBrokerClient

    @Shared
    boolean initialized

    protected String appBaseUrl = 'http://localhost:8080'

    protected String serviceDefinitionUrl = appBaseUrl + "/service-definition/{id}"
    protected String cfExtEndpointUrl = appBaseUrl + "/v2/cf-ext/{service_instance_id}/endpoint"

    protected String cfExtUser
    protected String cfExtPassword

    @Autowired
    private AuthenticationConfig authenticationConfig

    @PostConstruct
    private void initConfig(){
        cfExtUser = authenticationConfig.cfExtUsername
        cfExtPassword = authenticationConfig.cfExtPassword
    }

    @Autowired
    void init(ServiceLifeCycler serviceLifeCycler) {
        if (!initialized) {
            this.serviceLifeCycler = serviceLifeCycler
            serviceBrokerClient = new ServiceBrokerClient(appBaseUrl, 'cc_admin', 'change_me')
            initialized = true
        }
    }
}
