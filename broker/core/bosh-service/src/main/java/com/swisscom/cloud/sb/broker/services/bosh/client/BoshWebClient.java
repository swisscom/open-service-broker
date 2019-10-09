package com.swisscom.cloud.sb.broker.services.bosh.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException;
import com.swisscom.cloud.sb.broker.services.bosh.BoshConfig;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.util.Base64Utils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.SslProvider;
import reactor.netty.tcp.TcpClient;

import javax.net.ssl.SSLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshDeployment.boshDeployment;
import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshDirectorTask.boshTask;
import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshWebClient.BoshUri.*;
import static java.lang.String.format;
import static java.net.URI.create;
import static java.util.Collections.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.util.CollectionUtils.toMultiValueMap;
import static org.springframework.web.reactive.function.client.ExchangeStrategies.builder;
import static reactor.core.publisher.Mono.just;

/**
 * A REST client of the <a href='https://bosh.io/docs/director-api-v1/'>BOSH Director API v1</a>.
 *
 * @see <a href='https://bosh.io/docs/director-api-v1/'>BOSH Director API v1</a>
 */
public class BoshWebClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoshWebClient.class);

    enum BoshUri {
        INFO("/info"),
        TASKS("/tasks"),
        TASK("/tasks/{id}"),
        TASK_OUTPUT("/tasks/{id}/output"),
        DEPLOYMENTS("/deployments"),
        DEPLOYMENT("/deployments/{id}"),
        CONFIGS("/configs"),
        CONFIG("/configs/{id}"),
        OAUTH_TOKEN("/oauth/token"),
        STEMCELLS("/stemcells"),
        RELEASES("/releases");


        private final String value;

        BoshUri(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }


    }

    private static final String CONTENT_TYPE = "Content-Type";

    private static final String CONTENT_TYPE_YAML = "text/yaml";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private final BoshConfig boshConfig;

    private final WebClient webClient;

    private BoshWebClient(BoshConfig boshConfig, WebClient webClient) {
        this.boshConfig = boshConfig;
        this.webClient = webClient;
    }

    public static BoshWebClient boshWebClient(String baseUrl,
                                              String directorUsername,
                                              char[] directorPassword) {
        try {
            return new BoshWebClient(buildBoshConfig(baseUrl, directorUsername, directorPassword),
                                     buildWebClient(baseUrl));
        } catch (Exception e) {
            throw new IllegalStateException("Can't create a BOSH client", e);
        }
    }

    private static BoshConfig buildBoshConfig(String baseUrl, String directorUsername, char[] directorPassword) {
        return new BoshConfig() {
            @Override
            public String getBoshDirectorBaseUrl() {
                return baseUrl;
            }

            @Override
            public String getBoshDirectorUsername() {
                return directorUsername;
            }

            @Override
            public String getBoshDirectorPassword() {
                return new String(directorPassword);
            }
        };
    }


    //FIXME It is using a InsecureTrustManagerFactory! ONLY FOR TESTING!

    private static WebClient buildWebClient(String baseUrl) throws SSLException {
        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();

        TcpClient tcpClient = TcpClient.create(ConnectionProvider.elastic("bosh_connection_pool"))
                                       .secure(SslProvider.builder().sslContext(sslContext).build())
                                       .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2_000)
                                       .doOnConnected(connection ->
                                                              connection.addHandlerLast(new ReadTimeoutHandler(2))
                                                                        .addHandlerLast(new WriteTimeoutHandler(2)));


        return WebClient.builder()
                        .baseUrl(baseUrl)
                        .clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                        .exchangeStrategies(configureBoshContentTypes())
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                        .build();
    }

    /**
     * Support text/html Content-Type for JSON parsing, because BOSH Director API sets wrong Content-Type
     *
     * @return configuration setting the correct content types
     * @see <a href='https://github.com/cloudfoundry/bosh/issues/1290'>Open BOSH issue</a>
     */
    private static ExchangeStrategies configureBoshContentTypes() {
        return builder().codecs(
                configurer -> {
                    configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(new ObjectMapper(),
                                                                                           APPLICATION_JSON,
                                                                                           TEXT_HTML,
                                                                                           TEXT_PLAIN));
                    configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(new ObjectMapper(),
                                                                                           APPLICATION_JSON,
                                                                                           TEXT_HTML,
                                                                                           TEXT_PLAIN));
                }
        ).build();
    }

    public BoshInfo fetchBoshInfo() {
        return webClient.get()
                        .uri(BoshUri.INFO.value())
                        .retrieve()
                        .bodyToMono(BoshInfo.class)
                        .onErrorResume(WebClientResponseException.class,
                                       t -> onError(t, format("Error accessing BOSH with %s@%s: %s",
                                                              boshConfig.getBoshDirectorUsername(),
                                                              boshConfig.getBoshDirectorBaseUrl(),
                                                              t)))
                        .block();
    }

    public BoshDeployment requestDeployment(String deploymentName, String deploymentConfigurationYaml) {
        LOGGER.trace("requestDeployment('{}', '{}')", deploymentName, deploymentConfigurationYaml);
        return getPostWithAuthorizationToken(DEPLOYMENTS.value())
                .flatMap(cl -> cl.header(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_YAML)
                                 .body(just(deploymentConfigurationYaml), String.class)
                                 .exchange()
                                 .flatMap(response -> {
                                     if (response.statusCode().is3xxRedirection()) {
                                         return Mono.just(boshDeployment()
                                                                  .name(deploymentName)
                                                                  .taskUri(create(response.headers()
                                                                                          .header(HttpHeaders.LOCATION)
                                                                                          .get(0)))
                                                                  .build());
                                     } else {
                                         return response.bodyToMono(String.class)
                                                        .flatMap(bodyString -> onError(response,
                                                                                       format("Error accessing BOSH with %s@%s: %s %s",
                                                                                              boshConfig.getBoshDirectorUsername(),
                                                                                              boshConfig.getBoshDirectorBaseUrl(),
                                                                                              response.statusCode(),
                                                                                              bodyString)));
                                     }
                                 }))
                .block();
    }

    public BoshDeployment deleteDeployment(String deploymentName) {
        LOGGER.trace("deleteDeployment({})", deploymentName);
        BoshDeployment result = getDeployment(deploymentName);
        Mono<ClientResponse> response = getDeleteWithAuthorizationToken(DEPLOYMENTS.value(), deploymentName)
                .flatMap(cl -> cl.exchange());
        return response
                .flatMap(r -> {
                    if (r.statusCode().is3xxRedirection()) {
                        return Mono.just(boshDeployment().from(result)
                                                         .name(deploymentName)
                                                         .taskUri(create(r.headers()
                                                                          .header(HttpHeaders.LOCATION)
                                                                          .get(0)))
                                                         .build());
                    } else {
                        return r.bodyToMono(String.class)
                                .flatMap(bodyString -> onError(r,
                                                               format("Error accessing BOSH with %s@%s: %s %s",
                                                                      boshConfig.getBoshDirectorUsername(),
                                                                      boshConfig.getBoshDirectorBaseUrl(),
                                                                      r.statusCode(),
                                                                      bodyString)));
                    }


                }).block();

    }

    public BoshDeployment getDeployment(String deploymentId) {
        LOGGER.trace("getDeployment('{}')", deploymentId);
        return getGetWithAuthorizationToken(DEPLOYMENT.value(), deploymentId)
                .flatMap(cl -> cl.retrieve()
                                 .bodyToMono(BoshDeployment.class))
                .block();
    }

    public BoshCloudConfig requestConfig(BoshCloudConfigRequest request) {
        LOGGER.trace("requestConfig('{}')", request);
        return getPostWithAuthorizationToken(CONFIGS.value())
                .flatMap(cl -> cl.body(just(request.toJson()), String.class)
                                 .exchange()
                                 .flatMap(response -> {
                                     if (response.statusCode() == HttpStatus.CREATED) {
                                         return response.bodyToMono(BoshCloudConfig.class);
                                     } else {
                                         return response.bodyToMono(String.class)
                                                        .flatMap(bodyString -> onError(response,
                                                                                       format("Error accessing BOSH with %s@%s: %s %s",
                                                                                              boshConfig.getBoshDirectorUsername(),
                                                                                              boshConfig.getBoshDirectorBaseUrl(),
                                                                                              response.statusCode(),
                                                                                              bodyString)));
                                     }
                                 }))
                .block();
    }

    public Collection<BoshCloudConfig> getConfigs() {
        return getGetWithAuthorizationToken(CONFIGS.value(), emptyMap())
                .flatMap(cl -> cl.retrieve()
                                 .bodyToFlux(BoshCloudConfig.class)
                                 .collectList())
                .block();
    }

    public Collection<BoshCloudConfig> getConfig(String name) {
        LOGGER.trace("getConfig('{}')", name);
        return getGetWithAuthorizationToken(CONFIGS.value(), singletonMap("name", singletonList(name)))
                .flatMap(cl -> cl.retrieve()
                                 .bodyToFlux(BoshCloudConfig.class)
                                 .collectList())
                .block();
    }

    public BoshCloudConfig deleteConfig(BoshCloudConfigRequest request) {
        LOGGER.trace("deleteConfig({})", request);
        Collection<BoshCloudConfig> result = getConfig(request.getName());
        if (!result.isEmpty()) {
            getDeleteWithAuthorizationToken(CONFIGS.value(),
                                            ImmutableMap.of("name", singletonList(request.getName()),
                                                            "type", singletonList(request.getType())))
                    .flatMap(cl -> cl.retrieve()
                                     .bodyToMono(Void.class))
                    .block();
            return result.iterator().next();
        }
        return BoshCloudConfig.EMPTY;
    }

    public BoshDirectorTask getTask(String taskId) {
        return getGetWithAuthorizationToken(TASK.value(), taskId)
                .flatMap(cl -> cl.retrieve()
                                 .bodyToMono(BoshDirectorTask.class))
                .block();
    }

    public BoshDirectorTask getTaskWithEvents(String taskId) {
        BoshDirectorTask task = getTask(taskId);
        Collection<BoshDirectorTask.Event> events =
                getGetWithAuthorizationToken(TASK_OUTPUT.value(),
                                             taskId,
                                             singletonMap("type", singletonList("event")))
                        .flatMap(
                                cl -> cl.retrieve()
                                        .bodyToFlux(BoshDirectorTask.Event.class)
                                        .sort()
                                        .collectList())
                        .block();
        return boshTask()
                .from(task)
                .events(events)
                .build();
    }

    public Collection<BoshDirectorTask> getTasksAssociatedWithDeployment(String deploymentName) {
        return getGetWithAuthorizationToken(TASKS.value(),
                                            singletonMap("deployment", singletonList(deploymentName)))
                .flatMap(cl -> cl.retrieve()
                                 .bodyToFlux(BoshDirectorTask.class)
                                 .collectList())
                .block();
    }


    public Collection<BoshDeployment> getDeployments() {
        return getGetWithAuthorizationToken(DEPLOYMENTS.value(), emptyMap())
                .flatMap(cl -> cl.retrieve()
                                 .bodyToFlux(BoshDeployment.class)
                                 .collectList())
                .block();
    }

    public Collection<BoshStemcell> getStemcells() {
        return getGetWithAuthorizationToken(STEMCELLS.value(), emptyMap())
                .flatMap(cl -> cl.retrieve()
                                 .bodyToFlux(BoshStemcell.class)
                                 .collectList())
                .block();
    }

    public Collection<BoshRelease> getReleases() {
        return getGetWithAuthorizationToken(RELEASES.value(), emptyMap())
                .flatMap(cl -> cl.retrieve()
                                 .bodyToFlux(BoshRelease.class)
                                 .collectList())
                .block();
    }

    //TODO At the moment is launching an exception but maybe we should return an error object
    private <T> Mono<T> onError(ClientResponse response, String message) {
        return throwException(response.statusCode(), message);
    }

    private <T> Mono<T> onError(WebClientResponseException t, String message) {
        return throwException(t.getStatusCode(), message + " -> " + t.getResponseBodyAsString());
    }

    private <T> Mono<T> throwException(HttpStatus httpStatus, String message) {
        if (httpStatus.is4xxClientError()) {
            throw new IllegalArgumentException(message);
        } else if (httpStatus.is5xxServerError()) {
            throw new IllegalStateException(message);
        } else {
            throw new ServiceBrokerException(message);
        }
    }

    private Mono<WebClient.RequestHeadersSpec> getGetWithAuthorizationToken(String path, String id) {
        return getGetWithAuthorizationToken(path, id, emptyMap());
    }

    private Mono<WebClient.RequestHeadersSpec> getGetWithAuthorizationToken(String path,
                                                                            String id,
                                                                            Map<String, List<String>> params) {
        return getAuthorizationToken().flatMap(t -> just(webClient.get()
                                                                  .uri(uriBuilder -> uriBuilder
                                                                          .path(path)
                                                                          .queryParams(toMultiValueMap(params))
                                                                          .build(id))
                                                                  .headers(h -> h.setBearerAuth(t))));
    }

    private Mono<WebClient.RequestHeadersSpec> getGetWithAuthorizationToken(String path,
                                                                            Map<String, List<String>> params) {
        return getAuthorizationToken().flatMap(t -> just(webClient.get()
                                                                  .uri(uriBuilder -> uriBuilder
                                                                          .path(path)
                                                                          .queryParams(toMultiValueMap(params))
                                                                          .build())
                                                                  .headers(h -> h.setBearerAuth(t))));
    }

    private Mono<WebClient.RequestBodySpec> getPostWithAuthorizationToken(String path) {
        return getAuthorizationToken().flatMap(t -> just(webClient.post()
                                                                  .uri(path)
                                                                  .headers(h -> h.setBearerAuth(t))));
    }

    private Mono<WebClient.RequestBodySpec> getPutWithAuthorizationToken(String path) {
        return getAuthorizationToken().flatMap(t -> just(webClient.put()
                                                                  .uri(path)
                                                                  .headers(h -> h.setBearerAuth(t))));
    }

    private Mono<WebClient.RequestHeadersSpec> getDeleteWithAuthorizationToken(String path, String id) {
        return getAuthorizationToken().flatMap(t -> just(webClient.delete()
                                                                  .uri(path + "/{id}", id)
                                                                  .headers(h -> h.setBearerAuth(t))));
    }

    private Mono<WebClient.RequestHeadersSpec> getDeleteWithAuthorizationToken(String path,
                                                                               Map<String, List<String>> params) {
        return getAuthorizationToken().flatMap(t -> just(webClient.delete()
                                                                  .uri(uriBuilder -> uriBuilder
                                                                          .path(path)
                                                                          .queryParams(toMultiValueMap(params))
                                                                          .build())
                                                                  .headers(h -> h.setBearerAuth(t))));
    }

    private Mono<String> getAuthorizationToken() {
        return getAuthorization().flatMap(response -> just(response.get("access_token").textValue()));
    }

    private Mono<JsonNode> getAuthorization() {
        return webClient.post()
                        .uri(getAuthorizationUrl())
                        .header(HttpHeaders.AUTHORIZATION, "Basic " + getBase64BoshDirectorCredentials())
                        .body(BodyInserters.fromFormData("grant_type", "client_credentials"))
                        .retrieve()
                        .bodyToMono(JsonNode.class);
    }

    private String getAuthorizationUrl() {
        return fetchBoshInfo().getUserAuthentication().getUrl() + "/oauth/token";
    }

    private String getBase64BoshDirectorCredentials() {
        return Base64Utils.encodeToString((format("%s:%s",
                                                  boshConfig.getBoshDirectorUsername(),
                                                  boshConfig.getBoshDirectorPassword())).getBytes());
    }


}
