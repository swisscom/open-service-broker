package com.swisscom.cf.broker.functional

import com.swisscom.cf.broker.config.AuthenticationConfig
import com.swisscom.cf.broker.util.ServiceLifeCycler
import com.swisscom.cloud.sb.client.ServiceBrokerClientExtended
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.client.RestTemplate
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
    protected ServiceBrokerClientExtended serviceBrokerClient

    @Shared
    boolean initialized

    protected String appBaseUrl = 'http://localhost:8080'

    protected String serviceDefinitionUrl = appBaseUrl + "/service-definition/{id}"

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
            serviceBrokerClient = new ServiceBrokerClientExtended(new RestTemplate(), appBaseUrl, 'cc_admin', 'change_me','cf_ext','change_me')
            initialized = true
        }
    }
}
