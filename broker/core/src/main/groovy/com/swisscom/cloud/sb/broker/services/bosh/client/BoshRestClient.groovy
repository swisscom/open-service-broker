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

import com.swisscom.cloud.sb.broker.services.bosh.BoshConfig
import com.swisscom.cloud.sb.broker.services.bosh.BoshResourceNotFoundException
import com.swisscom.cloud.sb.broker.services.bosh.resources.BoshConfigResponse
import com.swisscom.cloud.sb.broker.services.bosh.resources.BoshInfo
import com.swisscom.cloud.sb.broker.services.bosh.resources.Task
import com.swisscom.cloud.sb.broker.services.bosh.resources.UaaLoginResponse
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import groovy.transform.PackageScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.util.Assert
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate

import static com.swisscom.cloud.sb.broker.services.bosh.GenericConfigAPIQueryFilter.createQueryFilter
import static org.springframework.http.HttpMethod.DELETE

@PackageScope
class BoshRestClient {
    private static final Logger LOG = LoggerFactory.getLogger(BoshRestClient.class);

    private static final String INFO = "/info";
    private static final String TASKS = "/tasks";
    private static final String DEPLOYMENTS = "/deployments";
    private static final String CONFIGS = "/configs";
    private static final String OAUTH_TOKEN = "/oauth/token";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_YAML = "text/yaml";
    private static final String CONTENT_TYPE_JSON = "application/json";

    private final BoshConfig boshConfig;
    private final RestTemplateBuilder restTemplateBuilder;

    BoshRestClient(BoshConfig boshConfig, RestTemplateBuilder restTemplateBuilder) {
        this.boshConfig = boshConfig;
        this.restTemplateBuilder = restTemplateBuilder;
    }

    BoshInfo fetchBoshInfo() {
        return createRestTemplate().getForEntity(prependBaseUrl(INFO), BoshInfo.class).getBody();
    }

    String postDeployment(String data) {
        LOG.trace("Posting new bosh deployment: \n${data}");
        HttpHeaders headers = new HttpHeaders()
        headers.add(CONTENT_TYPE, CONTENT_TYPE_YAML);
        HttpEntity<String> request = new HttpEntity<>(data, headers);
        ResponseEntity responseEntity = createAuthRestTemplate().exchange(prependBaseUrl(DEPLOYMENTS),
                                                                          HttpMethod.POST,
                                                                          request,
                                                                          Void.class);
        return handleRedirectonAndExtractTaskId(responseEntity);
    }

    String deleteDeployment(String id) {
        ResponseEntity response = createAuthRestTemplate().
                exchange(prependBaseUrl(DEPLOYMENTS + '/' + id) + "?force=true",
                         DELETE,
                         null,
                         Void.class);
        return handleRedirectonAndExtractTaskId(response);
    }

    Task getTask(String id) {
        return createAuthRestTemplate().exchange(prependBaseUrl(TASKS + '/' + id),
                                                 HttpMethod.GET,
                                                 null,
                                                 Task.class).getBody();
    }

    BoshConfigResponse postConfig(String config) {
        LOG.debug("Posting new config: \n${config}");
        HttpHeaders headers = new HttpHeaders()
        headers.add(CONTENT_TYPE, CONTENT_TYPE_JSON);
        HttpEntity<String> request = new HttpEntity<>(config, headers);
        return createAuthRestTemplate().exchange(prependBaseUrl(CONFIGS),
                                                 HttpMethod.POST,
                                                 request,
                                                 BoshConfigResponse.class)
                                       .getBody();
    }

    void deleteConfig(String name, String type) {
        createAuthRestTemplate().
                exchange(prependBaseUrl(CONFIGS + createQueryFilter().name(name).type(type).build().asUriString()),
                         DELETE,
                         null,
                         String.class);
    }

    private Optional<String> checkAuthTypeAndLogin() {
        BoshInfo info = fetchBoshInfo();
        Assert.notNull(info, "bosh info response must not be null")
        if (info.getUserAuthentication().getType().equals("uaa")) {
            return Optional.of(uaaLogin((String) info.getUserAuthentication()
                                                     .getOptions()
                                                     .get("url")).getAccessToken());
        }
        return Optional.empty();
    }

    private UaaLoginResponse uaaLogin(String uaaBaseUrl) {
        Assert.hasText(uaaBaseUrl, "uaaBaseUrl must be set")
        String loginUrl = uaaBaseUrl + OAUTH_TOKEN + "?grant_type=client_credentials"
        LOG.info("Authenticating against uaa with URL '${loginUrl}' and username '${boshConfig.getBoshDirectorUsername()}'")
        UaaLoginResponse result = createBasicAuthRestTemplate(boshConfig.getBoshDirectorUsername(),
                                                              boshConfig.getBoshDirectorPassword()).
                exchange(loginUrl,
                         HttpMethod.GET,
                         null,
                         UaaLoginResponse.class).
                getBody();
        Assert.notNull(result, "empty uaa login response is invalid")
        return result
    }

    private String handleRedirectonAndExtractTaskId(ResponseEntity response) {
        if (HttpStatus.FOUND != response.getStatusCode()) {
            throw new RuntimeException("Should have returned a 302, instead it got:${response.statusCode}");
        }
        URI uri = response.getHeaders().getLocation();
        if (uri == null) {
            throw new RuntimeException("There should be a redirect location");
        }
        LOG.info("PostDeployment response redirection uri: '${uri.toString()}'");
        return uri.getPath().substring(uri.getPath().lastIndexOf(TASKS) + TASKS.length());
    }

    private String prependBaseUrl(String path) {
        return boshConfig.getBoshDirectorBaseUrl() + path;
    }

    private RestTemplateBuilder createRestTemplateBuilder() {
        return restTemplateBuilder.withSSLValidationDisabled()
    }

    private RestTemplate createRestTemplate() {
        RestTemplate restTemplate = createRestTemplateBuilder().build();
        return addCustomRestTemplateConfig(restTemplate);
    }

    private RestTemplate createAuthRestTemplate() {
        Optional<String> token = checkAuthTypeAndLogin()
        def restTemplate = token.isPresent() ? createBearerAuthRestTemplate(token.get()) :
                           createBasicAuthRestTemplate(boshConfig.getBoshDirectorUsername(),
                                                       boshConfig.getBoshDirectorPassword())
        return addCustomRestTemplateConfig(restTemplate);
    }

    private RestTemplate createBasicAuthRestTemplate(String username, String password) {
        Assert.hasText(username, "username must be set")
        Assert.hasText(password, "password must be set")
        LOG.info("Authenticating with username '${username}'")
        return createRestTemplateBuilder().
                withBasicAuthentication(username, password).
                build()
    }

    private RestTemplate createBearerAuthRestTemplate(String token) {
        LOG.info("Authenticating with Bearer token (length ${token.length()})")
        return createRestTemplateBuilder().withBearerAuthentication(token).build()
    }

    private RestTemplate addCustomRestTemplateConfig(RestTemplate restTemplate) {
        restTemplate.setErrorHandler(new CustomErrorHandler());

        // Support text/html Content-Type for JSON parsing, because BOSH Director API sets wrong Content-Type
        // Open Issue: https://github.com/cloudfoundry/bosh/issues/1290
        HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_HTML));
        restTemplate.getMessageConverters().add(converter);
        return restTemplate
    }

    private class CustomErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            // your error handling here
            if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new BoshResourceNotFoundException(
                        "Bosh resource not found, response body:${response.body?.toString()}",
                        null,
                        null,
                        HttpStatus.NOT_FOUND);
            }
            super.handleError(response);
        }
    }
}
