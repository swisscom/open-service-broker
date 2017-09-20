package com.swisscom.cloud.sb.test.httpserver

import org.apache.catalina.connector.Connector
import org.apache.coyote.http11.Http11NioProtocol
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ConnectorConfig {

    private final HttpServerConfig httpServerConfig

    @Autowired
    ConnectorConfig(HttpServerConfig httpServerConfig) {
        this.httpServerConfig = httpServerConfig
    }

    @Bean
    EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory()
        if (httpServerConfig.httpsPort) {
            tomcat.addAdditionalTomcatConnectors(createSslConnector())
        }
        return tomcat
    }

    private Connector createSslConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol")
        Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler()
        try {
            connector.setScheme("https")
            connector.setSecure(true)
            connector.setPort(httpServerConfig.httpsPort)
            protocol.setSSLEnabled(true)
            if (httpServerConfig.keyStorePath == null || httpServerConfig.keyStorePassword == null || httpServerConfig.keyStoreAlias == null) {
                throw new RuntimeException("Invalid configuration")
            }
            protocol.setKeystoreFile(httpServerConfig.keyStorePath)
            protocol.setKeystorePass(httpServerConfig.keyStorePassword)
            protocol.setKeyAlias(httpServerConfig.keyStoreAlias)


            if (httpServerConfig.trustStorePath && httpServerConfig.trustStorePassword) {
                protocol.truststoreFile = httpServerConfig.trustStorePath
                protocol.truststorePass = httpServerConfig.trustStorePassword
                protocol.clientAuth = 'true'
            }
            return connector
        }
        catch (IOException ex) {
            throw new IllegalStateException("can't access keystore: [" + "keystore"
                    + "] or truststore: [" + "keystore" + "]", ex)
        }
    }
}
