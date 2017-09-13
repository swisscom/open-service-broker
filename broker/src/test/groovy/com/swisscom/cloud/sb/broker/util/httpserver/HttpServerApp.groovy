package com.swisscom.cloud.sb.broker.util.httpserver

import groovy.util.logging.Slf4j
import org.apache.catalina.startup.TldConfig
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

@SpringBootApplication(scanBasePackageClasses = com.swisscom.cloud.sb.broker.util.httpserver.HttpServerApp.class, exclude = [ServiceBrokerAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JmxAutoConfiguration.class,
        TldConfig.class])
@Configuration
@Slf4j
class HttpServerApp {
    private ConfigurableApplicationContext context
    private HttpServerConfig httpServerConfig
    private static int DEFAULT_PORT = 35000


    static void main(String[] args) {
        new HttpServerApp().startServer(HttpServerConfig.create(DEFAULT_PORT))
    }

    def startServer(HttpServerConfig serverConfig) {
        this.httpServerConfig = serverConfig

        HashMap<String, Object> props = new HashMap<>()
        props."server.port" = serverConfig.port
        props."spring.jmx.enabled" = false

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



