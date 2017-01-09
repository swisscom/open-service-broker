package com.swisscom.cf.broker.services.bosh.client

import com.swisscom.cf.broker.services.bosh.BoshConfig
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
class BoshRestClient {

    public static final String INFO = '/info'
    public static final String TASKS = '/tasks'
    public static final String DEPLOYMENTS = '/deployments'
    public static final String VMS = '/vms'
    public static final String CLOUD_CONFIGS = '/cloud_configs'

    public static final String CONTENT_TYPE_YAML = "text/yaml"

    private final BoshConfig boshConfig
    private final RestTemplateFactory restTemplateFactory

    static BoshRestClient create(BoshConfig boshConfig, RestTemplateFactory restBuilderFactory) {
        return new BoshRestClient(boshConfig, restBuilderFactory)
    }

    private BoshRestClient(BoshConfig boshConfig, RestTemplateFactory restTemplateFactory) {
        this.boshConfig = boshConfig
        this.restTemplateFactory = restTemplateFactory
    }

    String getBoshInfo() {
        createRestTemplate().getForEntity(prependBaseUrl(INFO),String.class).body
    }

    String getDeployment(String id) {
        createRestTemplate().exchange(prependBaseUrl(DEPLOYMENTS + '/' + id),
                HttpMethod.GET, new HttpEntity(createAuthHeaders()), String.class).body
    }

    private HttpHeaders createAuthHeaders() {
        return createSimpleAuthHeaders(boshConfig.boshDirectorUsername, boshConfig.boshDirectorPassword)
    }


    String postDeployment(String data) {
        log.trace("Posting new bosh deployment: \n${data}")
        HttpHeaders headers = createAuthHeaders()
        headers.add('Content-Type',CONTENT_TYPE_YAML)
        HttpEntity<String> request = new HttpEntity<>(data,headers)

        def responseEntity = createRestTemplate().exchange(prependBaseUrl(DEPLOYMENTS),HttpMethod.POST,request,String.class)

        return handleRedirectonAndExtractTaskId(responseEntity)
    }

    private String handleRedirectonAndExtractTaskId(ResponseEntity response) {
        if (HttpStatus.FOUND != response.statusCode) {
            throw new RuntimeException("Should have returned a 302, instead it got:${response.statusCode}")
        }
        def uri = response.headers.getLocation()
        if (!uri) {
            throw new RuntimeException("There should be a redirect location")
        }
        log.info("PostDeployment response redirection uri:${uri.toString()}")
        return uri.path.substring(uri.path.lastIndexOf(TASKS) + TASKS.size())
    }

    String deleteDeployment(String id) {
        def response = createRestTemplate().exchange(prependBaseUrl(DEPLOYMENTS + '/' + id),HttpMethod.DELETE,new HttpEntity<Object>(createAuthHeaders()),String.class)
        return handleRedirectonAndExtractTaskId(response)
    }

    String getCloudConfig() {
        createRestTemplate().exchange(prependBaseUrl(CLOUD_CONFIGS + "?limit=1"),HttpMethod.GET,new HttpEntity<Object>(createAuthHeaders()),String.class).body
    }

    void postCloudConfig(String data) {
        log.trace("Updating cloud config with: ${data}")
        HttpHeaders headers = createAuthHeaders()
        headers.add('Content-Type',CONTENT_TYPE_YAML)
        HttpEntity<String> request = new HttpEntity<>(data,headers)
        createRestTemplate().exchange(prependBaseUrl(CLOUD_CONFIGS),HttpMethod.POST,request,Void.class)
    }

    String getTask(String id) {
        createRestTemplate().exchange(prependBaseUrl(TASKS + '/' + id),HttpMethod.GET,new HttpEntity( createAuthHeaders()), String.class).body
    }

    private String prependBaseUrl(String path) {
        return boshConfig.boshDirectorBaseUrl + path
    }

    BoshConfig getBoshConfig() {
        return boshConfig
    }

    RestTemplateFactory getRestTemplateFactory() {
        return restTemplateFactory
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
                throw new BoshResourceNotFoundException("Bosh resource not found, response body:${response.body?.toString()}", null, null, HttpStatus.NOT_FOUND.value())
            }
            super.handleError(response)
        }
    }
}
