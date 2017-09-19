package com.swisscom.cloud.sb.test.httpserver

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.cloud.servicebroker.config.ServiceBrokerAutoConfiguration
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Configuration

@SpringBootApplication(scanBasePackageClasses = com.swisscom.cloud.sb.test.httpserver.HttpServerApp.class, exclude = [ServiceBrokerAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JmxAutoConfiguration.class])
@Configuration
@Slf4j
@CompileStatic
class HttpServerApp {
    private ConfigurableApplicationContext context
    private HttpServerConfig httpServerConfig
    private static int DEFAULT_HTTP_PORT = 35000
    private static int DEFAULT_HTTPS_PORT = 35001


    static void main(String[] args) {
        new HttpServerApp().startServer(HttpServerConfig.create(DEFAULT_HTTP_PORT).withHttpsPort(DEFAULT_HTTPS_PORT)
                .withKeyStore(HttpServerApp.getClass().getResource('/server-keystore.jks').file, 'secret', 'secure-server')
                .withTrustStore(HttpServerApp.getClass().getResource('/server-truststore.jks').file, 'secret'))
    }

    def startServer(HttpServerConfig serverConfig) {
        this.httpServerConfig = serverConfig

        HashMap<String, Object> props = new HashMap<>()
        props."server.port" = serverConfig.httpPort
        props."spring.jmx.enabled" = false
        props."logging.level.org.springframework.security" = 'DEBUG'

        context = new SpringApplicationBuilder()
                .sources(HttpServerApp.class)
                .properties(props)
                .initializers(new ApplicationContextInitializer<ConfigurableApplicationContext>() {
            @Override
            void initialize(ConfigurableApplicationContext applicationContext) {
                applicationContext.getBeanFactory().registerResolvableDependency(HttpServerConfig.class, httpServerConfig)

            }
        })
                .run(new String[0])

        return this
    }

    def stop() {
        SpringApplication.exit(context)
    }
}



