package com.swisscom.cloud.sb.broker.services.bosh.impl;

import com.google.common.collect.ImmutableMap;
import com.swisscom.cloud.sb.broker.services.bosh.BoshDirectorService;
import com.swisscom.cloud.sb.broker.services.bosh.client.*;
import com.swisscom.cloud.sb.broker.template.TemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshCloudConfigRequest.configRequest;
import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshWebClient.boshWebClient;
import static java.lang.String.format;

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
    private final TemplateEngine templateEngine;
    private final String defaultBoshDeploymentTemplateId;

    private WebClientBoshDirectorService(BoshWebClient boshWebClient,
                                         TemplateEngine templateEngine,
                                         String defaultBoshDeploymentTemplateId) {
        this.boshWebClient = boshWebClient;

        this.templateEngine = templateEngine;
        this.defaultBoshDeploymentTemplateId = defaultBoshDeploymentTemplateId;
    }

    public static WebClientBoshDirectorService of(BoshWebClient boshWebClient,
                                                  TemplateEngine templateEngine,
                                                  String defaultBoshDeploymentTemplateId) {
        return new WebClientBoshDirectorService(boshWebClient,
                                                templateEngine,
                                                defaultBoshDeploymentTemplateId);
    }

    public static WebClientBoshDirectorService of(String boshDirectorBaseUrl,
                                                  String boshDirectorUsername,
                                                  char[] boshDirectorPassword,
                                                  TemplateEngine templateEngine,
                                                  String defaultBoshDeploymentTemplateId) {
        return of(boshWebClient(boshDirectorBaseUrl, boshDirectorUsername, boshDirectorPassword),
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
    public BoshCloudConfig requestBoshCloudConfig(BoshCloudConfig boshCloudConfig) {
        checkArgument(!isNullOrEmpty(boshCloudConfig.getName().trim()),
                      "Can't request a BoshCloudConfig with empty name");
        return boshWebClient.requestConfig(configRequest().name(boshCloudConfig.getName())
                                                          .type(boshCloudConfig.getType())
                                                          .content(processBoshCloudConfigTemplate(boshCloudConfig))
                                                          .build());
    }

    @Override
    public BoshCloudConfig deleteBoshConfig(String name) {
        checkArgument(!isNullOrEmpty(name), NO_DELETE_WITH_NULL, BOSH_CLOUD_CONFIG, "name");
        return boshWebClient.deleteConfig(configRequest().name(name).type("cloud").build());
    }

    @Override
    public BoshDeployment requestBoshDeployment(BoshDeploymentRequest boshDeploymentRequest, String templateId) {
        return BoshDeployment.boshDeployment().from(
                this.boshWebClient.requestDeployment(format(BOSH_DEPLOYMENT_FORMAT,
                                                            BOSH_DEPLOYMENT_PREFIX,
                                                            boshDeploymentRequest.getName()),
                                                     processBoshDeploymentTemplate(
                                                             boshDeploymentRequest,
                                                             templateId)))
                             .boshDeploymentRequest(boshDeploymentRequest)
                             .build();
    }

    @Override
    public BoshDeployment requestBoshDeployment(BoshDeploymentRequest request) {
        return requestBoshDeployment(request, defaultBoshDeploymentTemplateId);
    }

    private String processBoshCloudConfigTemplate(BoshCloudConfig boshCloudConfig) {
        return templateEngine.process("bosh-cloud-config.ftlh",
                                      ImmutableMap.<String, Object>builder()
                                              .put("cloudConfig", boshCloudConfig)
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
        return boshWebClient.deleteDeployment(name);
    }

}
