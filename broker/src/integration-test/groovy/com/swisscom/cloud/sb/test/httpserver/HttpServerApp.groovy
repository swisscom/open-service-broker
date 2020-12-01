/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.test.httpserver

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.cloud.servicebroker.autoconfigure.web.ServiceBrokerAutoConfiguration
import org.springframework.cloud.servicebroker.autoconfigure.web.servlet.ServiceBrokerWebMvcAutoConfiguration
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Configuration

@SpringBootApplication(scanBasePackageClasses = HttpServerApp.class,
        exclude = [ServiceBrokerWebMvcAutoConfiguration.class, ServiceBrokerAutoConfiguration.class, FlywayAutoConfiguration.class])
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
                .withKeyStore(HttpServerApp.class.getResource('/server-keystore.jks').file, 'secret', 'secure-server')
                .withTrustStore(HttpServerApp.class.getResource('/server-truststore.jks').file, 'secret'))
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



