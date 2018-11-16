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

package com.swisscom.cloud.sb.broker.util

import groovy.transform.CompileStatic
import groovy.transform.Synchronized
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.AuthCache
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.HttpClient
import org.apache.http.client.protocol.ClientContext
import org.apache.http.conn.ssl.TrustStrategy
import org.apache.http.conn.ssl.X509HostnameVerifier
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.impl.client.BasicAuthCache
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.ProxyAuthenticationStrategy
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.protocol.HttpContext
import org.apache.http.ssl.SSLContexts
import org.bouncycastle.openssl.PEMReader
import org.springframework.context.annotation.Scope
import org.springframework.http.HttpMethod
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.client.support.BasicAuthorizationInterceptor
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

import javax.net.ssl.SSLContext
import javax.net.ssl.SSLException
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSocket
import java.security.KeyPair
import java.security.KeyStore
import java.security.Security
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

@Component('RestTemplateBuilder')
@CompileStatic
@Scope("prototype")
class RestTemplateBuilder {

    protected RestTemplate restTemplate
    protected HttpClientBuilder httpClientBuilder
    private boolean useDigestAuth = false
    private org.apache.http.ssl.TrustStrategy trustStrategy
    private KeyStore keyStore
    private boolean disableHostNameVerification

    RestTemplateBuilder() {
        restTemplate = new RestTemplate()
        httpClientBuilder = HttpClientBuilder.create()
    }

    RestTemplate build() {
        httpClientBuilder.setSSLContext(createSSLContext())
        if (disableHostNameVerification) {
            httpClientBuilder.setHostnameVerifier(DummyHostnameVerifier.INSTANCE)
        }
        def httpClientRequestFactory = (useDigestAuth) ? new HttpComponentsClientHttpRequestFactoryDigestAuth(httpClientBuilder.build()) : new HttpComponentsClientHttpRequestFactory(httpClientBuilder.build())
        addLoggingRequestInterceptor()
        restTemplate.setRequestFactory(httpClientRequestFactory)
        return this.restTemplate
    }

    @Synchronized
    private void addLoggingRequestInterceptor() {
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors()
        if (interceptors == null) {
            interceptors = Collections.emptyList()
        } else {
            interceptors.removeAll { it instanceof LoggingRequestInterceptor }
        }
        interceptors.add(new LoggingRequestInterceptor())
        restTemplate.setInterceptors(interceptors)
    }

    private SSLContext createSSLContext() {
        def contextBuilder = SSLContexts.custom()
        if (keyStore) {
            contextBuilder.loadKeyMaterial(keyStore, null)
        }
        if (trustStrategy) {
            contextBuilder.loadTrustMaterial(new TrustAnyCertificateStrategy())
        }

        return contextBuilder.build()
    }

    RestTemplateBuilder withBasicAuthentication(String username, String password) {
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors()
        if (interceptors == null) {
            interceptors = Collections.emptyList()
        } else {
            interceptors.removeAll { it instanceof BasicAuthorizationInterceptor }
        }
        interceptors.add(new BasicAuthorizationInterceptor(username, password))
        restTemplate.setInterceptors(interceptors)
        this
    }

    RestTemplateBuilder withDigestAuthentication(String user, String password) {
        useDigestAuth = true
        httpClientBuilder.setDefaultCredentialsProvider(provider(user, password)).useSystemProperties()
        this
    }

    RestTemplateBuilder withSSLValidationDisabled() {
        trustStrategy = TrustAnyCertificateStrategy.INSTANCE
        this
    }

    RestTemplateBuilder withHostNameVerificationDisabled() {
        disableHostNameVerification = true
        this
    }

    RestTemplateBuilder withClientSideCertificate(String cert, String key) {
        keyStore = createKeyStore(cert, key)
        this
    }

    RestTemplateBuilder withProxy(String proxyHost, String proxyPort, String proxyProtocol) {
        httpClientBuilder.setProxy(new HttpHost(proxyHost, Integer.parseInt(proxyPort), proxyProtocol))
        this
    }

    RestTemplateBuilder withAuthenticatedProxy(String proxyHost, String proxyPort, String proxyProtocol, String proxyUser, String proxyPassword) {
        withProxy(proxyHost, proxyPort, proxyProtocol)
        CredentialsProvider credsProvider = new BasicCredentialsProvider()
        credsProvider.setCredentials(new AuthScope(proxyHost, Integer.parseInt(proxyPort)), new UsernamePasswordCredentials(proxyUser, proxyPassword))
        httpClientBuilder.setDefaultCredentialsProvider(credsProvider)
        httpClientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy())
        this
    }

    private KeyStore createKeyStore(String certificate, String key) {
        def keyStore = KeyStore.getInstance("PKCS12")
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider())
        X509Certificate cert = (X509Certificate) (new PEMReader((new StringReader(certificate)))).readObject()
        keyStore.load(null, "".toCharArray())
        keyStore.setCertificateEntry("", cert)
        keyStore.setKeyEntry("1", ((KeyPair) (new PEMReader(new StringReader(key))).readObject()).getPrivate(),
                "".toCharArray(),
                [cert].toArray(new Certificate[0]))
        return keyStore
    }

    private CredentialsProvider provider(String user, String password) {
        CredentialsProvider provider = new BasicCredentialsProvider()
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, password)
        provider.setCredentials(AuthScope.ANY, credentials)
        return provider
    }

    private
    static class HttpComponentsClientHttpRequestFactoryDigestAuth extends HttpComponentsClientHttpRequestFactory {
        HttpComponentsClientHttpRequestFactoryDigestAuth(HttpClient client) {
            super(client)
        }

        @Override
        protected HttpContext createHttpContext(HttpMethod httpMethod, URI uri) {
            AuthCache authCache = new BasicAuthCache()
            BasicScheme basicAuth = new BasicScheme()
            HttpHost targetHost = new HttpHost(uri.getHost(), uri.getPort())
            authCache.put(targetHost, basicAuth)
            BasicHttpContext localcontext = new BasicHttpContext()
            localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache)
            return localcontext
        }
    }

    private static class TrustAnyCertificateStrategy implements TrustStrategy {
        public static final TrustAnyCertificateStrategy INSTANCE = new TrustAnyCertificateStrategy()

        @Override
        boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            return true
        }
    }

    private static class DummyHostnameVerifier implements X509HostnameVerifier {
        public static final DummyHostnameVerifier INSTANCE = new DummyHostnameVerifier()

        @Override
        void verify(String host, SSLSocket ssl) throws IOException {

        }

        @Override
        void verify(String host, X509Certificate cert) throws SSLException {

        }

        @Override
        void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {

        }

        @Override
        boolean verify(String s, SSLSession sslSession) {
            return true
        }
    }

}
