package com.swisscom.cloud.sb.broker.services.kubernetes.client.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.swisscom.cloud.sb.broker.services.kubernetes.config.KubernetesConfig
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import jdk.nashorn.internal.runtime.logging.Logger
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContexts
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

import javax.net.ssl.SSLContext
import java.security.KeyStore

@CompileStatic
@Component
@Logger
@Log4j
class KubernetesClient<RESPONSE> {

    KubernetesConfig kubernetesConfig
    RestTemplate restTemplate

    @Autowired
    KubernetesClient(KubernetesConfig kubernetesConfig) {
        this.kubernetesConfig = kubernetesConfig
        this.restTemplate = new RestTemplate()
    }


    ResponseEntity<RESPONSE> exchange(String url, HttpMethod method,
                                      String body, Class<RESPONSE> responseType) {
        enableSSLWithClientCertificate()
        log.info(url + " - " + convertYamlToJson(body))
        return restTemplate.exchange(
                "https://" + kubernetesConfig.getKubernetesHost() + ":" + kubernetesConfig.getKubernetesPort() + "/" +
                        url, method, new HttpEntity<String>(convertYamlToJson(body), getJsonHeaders()), responseType)
    }

    private HttpHeaders getJsonHeaders() {
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        return headers
    }

    private void enableSSLWithClientCertificate() {
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(HttpClients.custom().setSSLContext(getSSLContext()).build()))
    }

    private SSLContext getSSLContext() {
        SSLContext sslContext = SSLContexts.custom()
                .loadKeyMaterial(getKeyStore(), null).loadTrustMaterial(new TrustSelfSignedStrategy())
                .build()
        return sslContext
    }

    private KeyStore getKeyStore() {
        KeyStore keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(new FileInputStream(kubernetesConfig.getKubernetesClientPFXPath()), kubernetesConfig.getKubernetesClientPFXPasswordPath().toCharArray())
        return keyStore
    }

    String convertYamlToJson(String yaml) {
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory())
        Object obj = yamlReader.readValue(yaml, Object.class)
        ObjectMapper jsonWriter = new ObjectMapper()
        return jsonWriter.writeValueAsString(obj)
    }

}