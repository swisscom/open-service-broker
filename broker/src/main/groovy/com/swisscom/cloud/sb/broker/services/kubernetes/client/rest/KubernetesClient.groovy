package com.swisscom.cloud.sb.broker.services.kubernetes.client.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.google.common.annotations.VisibleForTesting
import com.swisscom.cloud.sb.broker.services.kubernetes.config.KubernetesConfig
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContexts
import org.bouncycastle.openssl.PEMReader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

import javax.net.ssl.SSLContext
import java.security.KeyPair
import java.security.KeyStore
import java.security.Security
import java.security.cert.Certificate
import java.security.cert.X509Certificate

@CompileStatic
@Component
@Log4j
class KubernetesClient<RESPONSE> {

    KubernetesConfig kubernetesConfig
    @VisibleForTesting
    RestTemplate restTemplate
//    @VisibleForTesting
//    KeyStore keyStore
    RestTemplateBuilder restTemplateBuilder

    @Autowired
    KubernetesClient(KubernetesConfig kubernetesConfig, RestTemplateBuilder restTemplateBuilder) {
        this.kubernetesConfig = kubernetesConfig
        this.restTemplateBuilder = restTemplateBuilder
//        keyStore = KeyStore.getInstance("PKCS12")
    }


    ResponseEntity<RESPONSE> exchange(String url, HttpMethod method,
                                      String body, Class<RESPONSE> responseType) {
        restTemplate = restTemplateBuilder.withMutualTLS(kubernetesConfig.kubernetesClientCertificate, kubernetesConfig.kubernetesClientKey).build()
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

//    void enableSSLWithClientCertificate() {
//        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(HttpClients.custom().setSSLContext(getSSLContext()).build()))
//    }
//
//    private SSLContext getSSLContext() {
//        SSLContext sslContext = SSLContexts.custom()
//                .loadKeyMaterial(getKeyStore(), null)
//                .loadTrustMaterial(new TrustSelfSignedStrategy())
//                .build()
//        return sslContext
//    }
//
//    private KeyStore getKeyStore() {
//        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider())
//        X509Certificate cert = (X509Certificate) (new PEMReader((new StringReader(kubernetesConfig.kubernetesClientCertificate)))).readObject()
//        keyStore.load(null, "".toCharArray())
//        keyStore.setCertificateEntry("", cert)
//        keyStore.setKeyEntry("1", ((KeyPair) (new PEMReader(new StringReader(kubernetesConfig.kubernetesClientKey))).readObject()).getPrivate(), "".toCharArray(), createCertChain(cert))
//        return keyStore
//    }
//
//    private Certificate[] createCertChain(X509Certificate cert) {
//        Certificate[] cer = new Certificate[1]
//        cer[0] = cert
//        cer
//    }

    String convertYamlToJson(String yaml) {
        if (yaml == null || yaml.isEmpty()) {
            return ""
        }
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory())
        Object obj = yamlReader.readValue(yaml, Object.class)
        ObjectMapper jsonWriter = new ObjectMapper()
        return jsonWriter.writeValueAsString(obj)
    }

}
