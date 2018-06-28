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

package com.swisscom.cloud.sb.broker.services.bosh.client

import com.google.common.annotations.VisibleForTesting
import com.swisscom.cloud.sb.broker.services.bosh.BoshConfig
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import groovy.util.logging.Slf4j
import org.springframework.http.*
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate

import static com.swisscom.cloud.sb.broker.util.HttpHelper.createBearerTokenAuthHeaders
import static com.swisscom.cloud.sb.broker.util.HttpHelper.createSimpleAuthHeaders

@CompileStatic
@Slf4j
class BoshRestClient {

    public static final String INFO = '/info'
    public static final String TASKS = '/tasks'
    public static final String DEPLOYMENTS = '/deployments'
    public static final String VMS = '/vms'
    public static final String CLOUD_CONFIGS = '/cloud_configs'
    public static final String OAUTH_TOKEN = '/oauth/token'

    public static final String CONTENT_TYPE_YAML = "text/yaml"
    public static final String CLOUD_CONFIG_QUERY = "?limit=1"

    private boolean checkedAuthType
    private String token
    private Calendar tokenExpirationTime

    private final BoshConfig boshConfig
    private final RestTemplateBuilder restTemplateBuilder

    BoshRestClient(BoshConfig boshConfig, RestTemplateBuilder restTemplateBuilder) {
        this.boshConfig = boshConfig
        this.restTemplateBuilder = restTemplateBuilder
    }

    String fetchBoshInfo() {
        createRestTemplate().getForEntity(prependBaseUrl(INFO),String.class).body
    }

    String getDeployment(String id) {
        createRestTemplate().exchange(prependBaseUrl(DEPLOYMENTS + '/' + id),
                HttpMethod.GET, new HttpEntity(createAuthHeaders()), String.class).body
    }

    private HttpHeaders createAuthHeaders() {
        if (!checkedAuthType || (checkedAuthType && checkTokenIsExpired())) checkAuthTypeAndLogin()
        if (token != null) {
            return createBearerTokenAuthHeaders(token)
        } else {
            return createSimpleAuthHeaders(boshConfig.boshDirectorUsername, boshConfig.boshDirectorPassword)
        }
    }

    @VisibleForTesting
    private boolean checkTokenIsExpired() {
        return tokenExpirationTime.before(Calendar.getInstance())
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    protected void checkAuthTypeAndLogin() {
        def jsonSlurper = new JsonSlurper()
        def info = jsonSlurper.parseText(fetchBoshInfo())
        if (info.user_authentication.type == "uaa") {
            uaaLogin(info.user_authentication.options.url)
        }
        checkedAuthType = true
    }

    @VisibleForTesting
    @TypeChecked(TypeCheckingMode.SKIP)
    private void uaaLogin(String uaaBaseUrl) {
        def jsonSlurper = new JsonSlurper()
        def authResponse = jsonSlurper.parseText(createRestTemplate().exchange(uaaBaseUrl + OAUTH_TOKEN + "?grant_type=client_credentials", HttpMethod.GET, new HttpEntity(createSimpleAuthHeaders(boshConfig.boshDirectorUsername, boshConfig.boshDirectorPassword)), String.class).body)
        token = authResponse.access_token
        tokenExpirationTime = Calendar.getInstance()
        tokenExpirationTime.add(Calendar.SECOND, authResponse.expires_in - 5)
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
        def response = createRestTemplate().exchange(prependBaseUrl(DEPLOYMENTS + '/' + id)+'?force=true',HttpMethod.DELETE,new HttpEntity<Object>(createAuthHeaders()),String.class)
        return handleRedirectonAndExtractTaskId(response)
    }

    String fetchCloudConfig() {
        createRestTemplate().exchange(prependBaseUrl(CLOUD_CONFIGS + CLOUD_CONFIG_QUERY),HttpMethod.GET,new HttpEntity<Object>(createAuthHeaders()),String.class).body
    }

    void postCloudConfig(String data) {
        log.trace("Updating cloud config with: ${data}")
        HttpHeaders headers = createAuthHeaders()
        headers.add('Content-Type',CONTENT_TYPE_YAML)
        HttpEntity<String> request = new HttpEntity<>(data,headers)
        createRestTemplate().exchange(prependBaseUrl(CLOUD_CONFIGS),HttpMethod.POST,request,Void.class)
    }

    String getTask(String id) {
        createRestTemplate().exchange(prependBaseUrl(TASKS + '/' + id),HttpMethod.GET,new HttpEntity(createAuthHeaders()), String.class).body
    }

    @VisibleForTesting
    private String prependBaseUrl(String path) {
        return boshConfig.boshDirectorBaseUrl + path
    }

    BoshConfig getBoshConfig() {
        return boshConfig
    }

    private RestTemplate createRestTemplate() {
        def restTemplate = restTemplateBuilder.withSSLValidationDisabled().build()
        restTemplate.setErrorHandler(new CustomErrorHandler())
        return restTemplate
    }

    private class CustomErrorHandler extends DefaultResponseErrorHandler {
        @Override
        void handleError(ClientHttpResponse response) throws IOException {
            // your error handling here
            if(response.statusCode == HttpStatus.NOT_FOUND){
                throw new BoshResourceNotFoundException("Bosh resource not found, response body:${response.body?.toString()}", null, null, HttpStatus.NOT_FOUND)
            }
            super.handleError(response)
        }
    }
}
