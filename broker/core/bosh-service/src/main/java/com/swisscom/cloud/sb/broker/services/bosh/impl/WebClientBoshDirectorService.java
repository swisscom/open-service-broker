package com.swisscom.cloud.sb.broker.services.bosh.impl;

import com.google.common.collect.ImmutableMap;
import com.swisscom.cloud.sb.broker.services.bosh.BoshDirectorService;
import com.swisscom.cloud.sb.broker.services.bosh.BoshTemplate;
import com.swisscom.cloud.sb.broker.services.bosh.client.*;
import com.swisscom.cloud.sb.broker.services.bosh.resources.GenericConfig;
import com.swisscom.cloud.sb.broker.services.common.TemplateConfig;
import com.swisscom.cloud.sb.broker.template.TemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.swisscom.cloud.sb.broker.services.bosh.BoshTemplate.boshTemplateOf;
import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshConfigRequest.configRequest;
import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshDeploymentRequest.deploymentRequest;
import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshWebClient.boshWebClient;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * An implementation of {@link BoshDirectorService} which uses {@link com.swisscom.cloud.sb.broker.services.bosh.client.BoshWebClient}
 * for implementing the interface.
 */
public class WebClientBoshDirectorService implements BoshDirectorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoshDirectorService.class);
    private static final String NO_REQUEST_WITH_NULL = "Can't request a %s with null %s";
    private static final String NO_DELETE_WITH_NULL = "Can't delete a %s with null '%s'";
    private static final String BOSH_DEPLOYMENT = BoshDeployment.class.getSimpleName();
    private static final String BOSH_CLOUD_CONFIG = BoshCloudConfig.class.getSimpleName();
    private static final String BOSH_DEPLOYMENT_PREFIX = "d";
    public static final String BOSH_DEPLOYMENT_FORMAT = "%s-%s";

    private final BoshWebClient boshWebClient;
    private final List<GenericConfig> genericConfigs;
    private final TemplateConfig templates;
    private final TemplateEngine templateEngine;
    private final String defaultBoshDeploymentTemplateId;

    private WebClientBoshDirectorService(BoshWebClient boshWebClient,
                                         List<GenericConfig> genericConfigs,
                                         TemplateConfig templates,
                                         TemplateEngine templateEngine,
                                         String defaultBoshDeploymentTemplateId) {
        this.boshWebClient = boshWebClient;
        this.genericConfigs = genericConfigs;
        this.templates = templates;
        this.templateEngine = templateEngine;
        this.defaultBoshDeploymentTemplateId = defaultBoshDeploymentTemplateId;
    }

    public static WebClientBoshDirectorService of(BoshWebClient boshWebClient,
                                                  List<GenericConfig> genericConfigs,
                                                  TemplateConfig templates,
                                                  TemplateEngine templateEngine,
                                                  String defaultBoshDeploymentTemplateId) {
        return new WebClientBoshDirectorService(boshWebClient,
                                                genericConfigs,
                                                templates,
                                                templateEngine,
                                                defaultBoshDeploymentTemplateId);
    }

    public static WebClientBoshDirectorService of(String boshDirectorBaseUrl,
                                                  String boshDirectorUsername,
                                                  char[] boshDirectorPassword,
                                                  List<GenericConfig> genericConfigs,
                                                  TemplateConfig templates,
                                                  TemplateEngine templateEngine,
                                                  String defaultBoshDeploymentTemplateId) {
        return of(boshWebClient(boshDirectorBaseUrl, boshDirectorUsername, boshDirectorPassword),
                  genericConfigs,
                  templates,
                  templateEngine,
                  defaultBoshDeploymentTemplateId);
    }

    @Override
    public BoshDirectorTask getBoshDirectorTask(String id) {
        checkArgument(!isNullOrEmpty(id), "Can't get a %s with null id", BoshDirectorTask.class.getSimpleName());
        return boshWebClient.getTaskWithEvents(id);
    }

    @Override
    public Collection<BoshDirectorTask> getBoshDirectorTasks(BoshDeployment boshDeployment) {
        checkArgument(boshDeployment != null,
                      "Can't get a collection of %s with a null %s",
                      BoshDirectorTask.class.getSimpleName(),
                      BoshDeployment.class.getSimpleName());
        return boshWebClient.getTasksAssociatedWithDeployment(boshDeployment.getName());
    }

    @Override
    public List<BoshCloudConfig> requestParameterizedBoshConfig(String boshCloudConfigName,
                                                                Map<String, String> parameters) {
        checkArgument(!isNullOrEmpty(boshCloudConfigName) && !isBlank(boshCloudConfigName),
                      NO_REQUEST_WITH_NULL,
                      BOSH_CLOUD_CONFIG,
                      "name");
        return genericConfigs.stream()
                             .map(requestBoshConfig(boshCloudConfigName, parameters))
                             .collect(toList());
    }

    private Function<GenericConfig, BoshCloudConfig> requestBoshConfig(String serviceInstanceGuid,
                                                                       Map<String, String> parameters) {
        return config -> boshWebClient.requestConfig(configRequest().name(serviceInstanceGuid)
                                                                    .type(config.getType())
                                                                    .content(getConfigContentFor(serviceInstanceGuid,
                                                                                                 parameters,
                                                                                                 config))
                                                                    .build());
    }

    @Override
    public BoshCloudConfig deleteBoshConfig(String name) {
        checkArgument(!isNullOrEmpty(name), NO_DELETE_WITH_NULL, BOSH_CLOUD_CONFIG, "name");
        return boshWebClient.deleteConfig(configRequest().name(name).type("cloud").build());
    }

    @Override
    public BoshDeployment requestBoshDeployment(BoshDeploymentRequest boshDeploymentRequest, String templateId) {
        return this.boshWebClient.requestDeployment(format(BOSH_DEPLOYMENT_FORMAT,
                                                           BOSH_DEPLOYMENT_PREFIX,
                                                           boshDeploymentRequest.getName()),
                                                    processBoshDeploymentTemplate(boshDeploymentRequest, templateId));
    }

    @Override
    public BoshDeployment requestBoshDeployment(BoshDeploymentRequest request) {
        return requestBoshDeployment(request, defaultBoshDeploymentTemplateId);
    }

    // TODO BOSH API receives a JSON, so we can'' follow exactly same strategy we did for deployments, which receive a yaml
    private String processBoshCloudConfigTemplate(BoshCloudConfig boshCloudConfig) {
        return templateEngine.process("bosh-cloud-config.ftlh",
                                      ImmutableMap.<String, Object>builder()
                                              .put("cloud_config", boshCloudConfig)
                                              .build());
    }

    private String processBoshDeploymentTemplate(BoshDeploymentRequest request, String templateId) {
        return templateEngine.process(templateId,
                                      ImmutableMap.<String, Object>builder()
                                              .put("deploy", request)
                                              .put("bosh_info", boshWebClient.fetchBoshInfo())
                                              .put("bosh_prefix", BOSH_DEPLOYMENT_PREFIX)
                                              .build());
    }

    @Override
    public BoshDeployment cancelBoshDeployment(BoshDeployment toCancel) {
        throw new UnsupportedOperationException("TO DO");
    }

    @Override
    public BoshDeployment deleteBoshDeploymentIfExists(String name) {
        checkArgument(!isNullOrEmpty(name), NO_DELETE_WITH_NULL, BOSH_DEPLOYMENT, "name");
        return boshWebClient.deleteDeployment(deploymentRequest().name(name).build());
    }

    private String getConfigContentFor(String serviceInstanceGuid,
                                       Map<String, String> parameters,
                                       GenericConfig config) {
        return getTemplate(config).replaceAllNamed("guid", serviceInstanceGuid)
                                  .replaceAllNamed(parameters)
                                  .build();
    }

    private BoshTemplate getTemplate(GenericConfig config) {
        return boshTemplateOf(templates.getFirstTemplateForServiceKey(config.getTemplateName()));
    }

}