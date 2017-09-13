package com.swisscom.cloud.sb.broker.util.http

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.cloud.servicebroker.config.ServiceBrokerAutoConfiguration
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication(scanBasePackageClasses = com.swisscom.cloud.sb.broker.util.http.TestApp.class, exclude = [ServiceBrokerAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JmxAutoConfiguration])
class TestApp {
    @RestController
    static class Controller {
        @RequestMapping("/")
        String index() {
            return "Test"
        }

    }
}



