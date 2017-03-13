package com.swisscom.cf.broker.services.ecs

import com.google.common.annotations.VisibleForTesting
import com.swisscom.cf.broker.services.bosh.client.BoshResourceNotFoundException
import com.swisscom.cf.broker.services.ecs.ECSConfig
import com.swisscom.cf.broker.util.RestTemplateFactory
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.springframework.http.*
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate

import static com.swisscom.cf.broker.util.HttpHelper.createSimpleAuthHeaders

@CompileStatic
@Log4j
class ECSRestClient {

    public static final String INFO = '/info'
    public static final String NAMESPACES = '/namespaces'

    public static final String CONTENT_TYPE_YAML = "text/yaml"
    public static final String CLOUD_CONFIG_QUERY = "?limit=1"

    private final ECSConfig ecsConfig
    private final RestTemplateFactory restTemplateFactory

    ECSRestClient(ECSConfig ecsConfig, RestTemplateFactory restTemplateFactory) {
        this.ecsConfig = ecsConfig
        this.restTemplateFactory = restTemplateFactory
    }

    String fetchECSInfo() {
        createRestTemplate().getForEntity(prependBaseUrl(INFO), String.class).body
    }

    String getNamespaces(String id) {
        createRestTemplate().exchange(prependBaseUrl(NAMESPACES + '/' + id),
                HttpMethod.GET, new HttpEntity(createAuthHeaders()), String.class).body
    }

    String postDeployment(String data) {
        log.trace("Posting new bosh deployment: \n${data}")
        HttpHeaders headers = createAuthHeaders()
        headers.add('Content-Type',CONTENT_TYPE_YAML)
        HttpEntity<String> request = new HttpEntity<>(data,headers)

        createRestTemplate().exchange(prependBaseUrl(NAMESPACES),HttpMethod.POST, request, String.class).body
    }
    ECSConfig getECSConfig() {
        return ecsConfig
    }

    RestTemplateFactory getRestTemplateFactory() {
        return restTemplateFactory
    }

    private HttpHeaders createAuthHeaders() {
        return createSimpleAuthHeaders(ecsConfig.ecsManagementUsername, ecsConfig.ecsManagementPassword)
    }


    @VisibleForTesting
    private String prependBaseUrl(String path) {
        return ecsConfig.ecsManagementBaseUrl + path
    }

    private RestTemplate createRestTemplate() {
        def result = restTemplateFactory.buildWithSSLValidationDisabled()
        result.setErrorHandler(new CustomErrorHandler())
        return result
    }

    private class CustomErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            // your error handling here
            if(response.statusCode == HttpStatus.NOT_FOUND){
                throw new BoshResourceNotFoundException("ECS resource not found, response body:${response.body?.toString()}", null, null, HttpStatus.NOT_FOUND)
            }
            super.handleError(response)
        }
    }
}
