package com.swisscom.cf.broker.util

import groovy.transform.CompileStatic
import org.apache.http.HttpHost
import org.apache.http.client.AuthCache
import org.apache.http.client.HttpClient
import org.apache.http.client.protocol.ClientContext
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.impl.client.BasicAuthCache
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.protocol.HttpContext
import org.springframework.http.HttpMethod
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory

@CompileStatic
class HttpComponentsClientHttpRequestFactoryDigestAuth extends HttpComponentsClientHttpRequestFactory {
    public HttpComponentsClientHttpRequestFactoryDigestAuth(HttpClient client) {
        super(client);
    }

    @Override
    protected HttpContext createHttpContext(HttpMethod httpMethod, URI uri) {
        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        HttpHost targetHost = new HttpHost(uri.getHost(), uri.getPort());
        authCache.put(targetHost, basicAuth);
        BasicHttpContext localcontext = new BasicHttpContext();
        localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
        return localcontext;
    }
}