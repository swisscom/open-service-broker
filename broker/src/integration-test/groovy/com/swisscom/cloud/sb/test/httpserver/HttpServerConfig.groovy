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

class HttpServerConfig {
    private final int httpPort
    private int httpsPort

    private String username
    private String password
    private AuthenticationType authenticationType = AuthenticationType.NONE
    private String keyStorePath
    private String keyStorePassword
    private String keyStoreAlias
    private String trustStorePath
    private String trustStorePassword
    private String bearerToken


    static enum AuthenticationType {
        NONE, SIMPLE, DIGEST, BEARER, MUTUAL
    }

    private HttpServerConfig(int httpPort) { this.httpPort = httpPort }

    static HttpServerConfig create(int port) {
        new HttpServerConfig(port)
    }

    HttpServerConfig withHttpsPort(int port) {
        this.httpsPort = port
        return this
    }

    HttpServerConfig withKeyStore(String keyStorePath, String keyStorePassword, String keyStoreAlias) {
        this.keyStorePath = keyStorePath
        this.keyStorePassword = keyStorePassword
        this.keyStoreAlias = keyStoreAlias
        return this
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

    HttpServerConfig withTrustStore(String trustStorePath, String trustStorePassword) {
        this.authenticationType = authenticationType.MUTUAL
        this.trustStorePath = trustStorePath
        this.trustStorePassword = trustStorePassword
        return this
    }

    HttpServerConfig withBearerAuthentication(String token) {
        this.authenticationType = authenticationType.BEARER
        this.bearerToken = token
        return this
    }

    int getHttpPort() {
        return httpPort
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

    int getHttpsPort() {
        return httpsPort
    }

    String getKeyStorePath() {
        return keyStorePath
    }

    String getKeyStorePassword() {
        return keyStorePassword
    }

    String getKeyStoreAlias() {
        return keyStoreAlias
    }

    String getTrustStorePath() {
        return trustStorePath
    }

    String getTrustStorePassword() {
        return trustStorePassword
    }
}
