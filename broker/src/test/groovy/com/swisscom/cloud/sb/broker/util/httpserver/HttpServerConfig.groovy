package com.swisscom.cloud.sb.broker.util.httpserver

class HttpServerConfig {
    private final int port

    private String username
    private String password
    private AuthenticationType authenticationType = AuthenticationType.NONE


    static enum AuthenticationType {
        NONE, SIMPLE, DIGEST
    }

    private HttpServerConfig(int port) { this.port = port }

    static HttpServerConfig create(int port) {
        new HttpServerConfig(port)
    }

    HttpServerConfig withSimpleHttpAuthentication(String username, String password) {
        this.authenticationType = authenticationType.SIMPLE
        this.username = username
        this.password = password
        return this
    }

    HttpServerConfig withDigestAuthentication(String user, String password) {
        this.authenticationType = authenticationType.DIGEST
        this.username = user
        this.password = password
        return this
    }

    int getPort() {
        return port
    }

    String getUsername() {
        return username
    }

    String getPassword() {
        return password
    }

    AuthenticationType getAuthenticationType() {
        return authenticationType
    }
}
