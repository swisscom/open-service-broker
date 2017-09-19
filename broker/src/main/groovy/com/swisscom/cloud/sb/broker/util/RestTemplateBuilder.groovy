package com.swisscom.cloud.sb.broker.util

import groovy.transform.CompileStatic
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.AuthCache
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.HttpClient
import org.apache.http.client.protocol.ClientContext
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.conn.ssl.TrustStrategy
import org.apache.http.conn.ssl.X509HostnameVerifier
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.impl.client.BasicAuthCache
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.HttpClientBuilder
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

    RestTemplateBuilder() {
        restTemplate = new RestTemplate()
        httpClientBuilder = HttpClientBuilder.create()
    }

    RestTemplate build() {
        def httpClientRequestFactory = (useDigestAuth) ? new HttpComponentsClientHttpRequestFactoryDigestAuth(httpClientBuilder.build()) : new HttpComponentsClientHttpRequestFactory(httpClientBuilder.build())
        restTemplate.setRequestFactory(httpClientRequestFactory)
        return this.restTemplate
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
        httpClientBuilder.setSSLSocketFactory(new SSLSocketFactory(new DummyTrustStrategy(), new DummyX509HostnameVerifier()))
        this
    }

    RestTemplateBuilder withMutualTLS(String cert, String key) {
        httpClientBuilder.setSSLContext(getSSLContext(cert, key))
        this
    }

    RestTemplateBuilder withClientSideKeystore(String keyStorePath, String keyStorePassword, String trustStorePath, String trustStorePassword) {
        httpClientBuilder.setSSLContext(SSLContexts.custom()
                .loadKeyMaterial(loadKeyStore(keyStorePath, keyStorePassword), keyStorePassword.toCharArray())
                .loadTrustMaterial(new File(trustStorePath), trustStorePassword.toCharArray(), TrustSelfSignedStrategy.INSTANCE)
                .build())
        this
    }

    private SSLContext getSSLContext(String cert, String key) {
        return SSLContexts.custom()
                .loadKeyMaterial(getKeyStore(cert, key), null)
                .loadTrustMaterial(new TrustSelfSignedStrategy())
                .build()
    }

    private KeyStore getKeyStore(String certificate, String key) {
        def keyStore = KeyStore.getInstance("PKCS12")
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider())
        X509Certificate cert = (X509Certificate) (new PEMReader((new StringReader(certificate)))).readObject()
        keyStore.load(null, "".toCharArray())
        keyStore.setCertificateEntry("", cert)
        keyStore.setKeyEntry("1", ((KeyPair) (new PEMReader(new StringReader(key))).readObject()).getPrivate(), "".toCharArray(), createCertChain(cert))
        return keyStore
    }

    private KeyStore loadKeyStore(String path, String password) {
        FileInputStream is = new FileInputStream(path)
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType())
        keystore.load(is, password.toCharArray())
        return keystore
    }

    private Certificate[] createCertChain(X509Certificate cert) {
        Certificate[] cer = new Certificate[1]
        cer[0] = cert
        cer
    }

    static class DummyTrustStrategy implements TrustStrategy {
        @Override
        boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            return true
        }
    }

    static class DummyX509HostnameVerifier implements X509HostnameVerifier {
        @Override
        public void verify(String host, SSLSocket ssl) throws IOException {

        }

        @Override
        public void verify(String host, X509Certificate cert) throws SSLException {

        }

        @Override
        public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {

        }

        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true
        }
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

}
