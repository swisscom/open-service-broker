package com.swisscom.cloud.sb.broker.util.http

import org.springframework.boot.SpringApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext

class SimpleHttpServer {
    private final int port

    private String username
    private String password
    private AuthenticationType authenticationType = AuthenticationType.NONE
    private ConfigurableApplicationContext context


    private static enum AuthenticationType {
        NONE, SIMPLE
    }

    private SimpleHttpServer(int port) { this.port = port }

    static SimpleHttpServer create(int port) {
        new SimpleHttpServer(port)
    }

    SimpleHttpServer withSimpleHttpAuthentication(String username, String password) {
        this.authenticationType = authenticationType.SIMPLE
        this.username = username
        this.password = password
        return this
    }

    SimpleHttpServer buildAndStart() {
        HashMap<String, Object> props = new HashMap<>()
        props."server.port" = port
        switch (authenticationType) {
            case AuthenticationType.NONE:
                props."security.basic.enabled" = false
                break
            case AuthenticationType.SIMPLE:
                props."security.basic.enabled" = true
                props."security.user.name" = username
                props."security.user.password" = password

                break
        }


        context = new SpringApplicationBuilder()
                .sources(TestApp.class)
                .properties(props)
                .run(new String[0])

        return this
    }

    def stop() {
        SpringApplication.exit(context)
    }
}
