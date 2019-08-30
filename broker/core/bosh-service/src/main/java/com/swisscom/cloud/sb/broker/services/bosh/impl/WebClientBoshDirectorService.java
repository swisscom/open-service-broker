package com.swisscom.cloud.sb.broker.services.bosh.impl;

import com.swisscom.cloud.sb.broker.model.Parameter;
import com.swisscom.cloud.sb.broker.services.bosh.BoshDirectorService;
import com.swisscom.cloud.sb.broker.services.bosh.BoshTemplate;
import com.swisscom.cloud.sb.broker.services.bosh.client.BoshCloudConfig;
import com.swisscom.cloud.sb.broker.services.bosh.client.BoshDeployment;
import com.swisscom.cloud.sb.broker.services.bosh.client.BoshDirectorTask;
import com.swisscom.cloud.sb.broker.services.bosh.client.BoshWebClient;
import com.swisscom.cloud.sb.broker.services.bosh.resources.GenericConfig;
import com.swisscom.cloud.sb.broker.services.common.TemplateConfig;
import com.swisscom.cloud.sb.broker.util.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import static org.apache.commons.lang.StringUtils.isEmpty;

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

    private final BoshWebClient boshWebClient;
    private final List<GenericConfig> genericConfigs;
    private final TemplateConfig templates;
    private final String boshManifestFolder;

    private WebClientBoshDirectorService(BoshWebClient boshWebClient,
                                         List<GenericConfig> genericConfigs,
                                         TemplateConfig templates, String boshManifestFolder) {
        this.boshWebClient = boshWebClient;
        this.genericConfigs = genericConfigs;
        this.templates = templates;
        this.boshManifestFolder = boshManifestFolder;
    }

    public static WebClientBoshDirectorService of(BoshWebClient boshWebClient,
                                                  List<GenericConfig> genericConfigs,
                                                  TemplateConfig templates,
                                                  String boshManifestFolder) {
        return new WebClientBoshDirectorService(boshWebClient, genericConfigs, templates, boshManifestFolder);
    }

    public static WebClientBoshDirectorService of(String boshDirectorBaseUrl,
                                                  String boshDirectorUsername,
                                                  char[] boshDirectorPassword,
                                                  List<GenericConfig> genericConfigs,
                                                  TemplateConfig templates,
                                                  String boshManifestFolder) {
        return of(boshWebClient(boshDirectorBaseUrl, boshDirectorUsername, boshDirectorPassword),
                  genericConfigs,
                  templates,
                  boshManifestFolder);
    }

    @Override
    public BoshDirectorTask getBoshDirectorTask(String id) {
        checkArgument(!isNullOrEmpty(id), "Can't get a %s with null id", BoshDirectorTask.class.getSimpleName());
        return boshWebClient.getTask(id);
    }

    @Override
    public Collection<BoshDirectorTask> getBoshDirectorTask(BoshDeployment boshDeployment) {
        checkArgument(boshDeployment != null,
                      "Can't get a collection of %s with a null %s",
                      BoshDirectorTask.class.getSimpleName(),
                      BoshDeployment.class.getSimpleName());
        return boshWebClient.getTaskAssociatedWithDeployment(boshDeployment.getName());
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
    public BoshDeployment requestParameterizedBoshDeployment(String serviceInstanceGuid,
                                                             String templateId,
                                                             Set<Parameter> parameters) {
        checkArgument(!isNullOrEmpty(serviceInstanceGuid),
                      NO_REQUEST_WITH_NULL,
                      BOSH_DEPLOYMENT,
                      "serviceInstanceGuid");
        checkArgument(parameters != null, NO_REQUEST_WITH_NULL, BOSH_DEPLOYMENT, "template parameters");
        return this.boshWebClient.requestDeployment(getDeploymentConfigContentFor(serviceInstanceGuid,
                                                                                  templateId,
                                                                                  parameters));
    }

    //FIXME CACHE BoshInfo uuid!!!
    private String getDeploymentConfigContentFor(String serviceInstanceGuid,
                                                 String templateId,
                                                 Set<Parameter> parameters) {
        return boshTemplateOf(readTemplateContent(templateId)).replaceAllNamed("guid", serviceInstanceGuid)
                                                              .replaceAllNamed("prefix", "d-")
                                                              .replaceAllNamed("bosh-director-uuid",
                                                                               boshWebClient.fetchBoshInfo().getUuid())
                                                              .replaceAllNamed(parameters)
                                                              .build();
    }

    private String readTemplateContent(String templateId) {
        String result = templates.getFirstTemplateForServiceKey(templateId, "1.0.0");
        return isEmpty(result) ? readDefaultTemplateContent(templateId) : result;
    }

    //TODO Change boshManifestFolder to be a Path!
    private String readDefaultTemplateContent(String templateId) {
        try {
            // Fallback method which was used by BOSH deployments
            String fileName = templateId.endsWith(".yml") ? templateId : templateId + ".yml";
            File file = new File(boshManifestFolder, fileName);
            if (file.exists()) {
                LOGGER.debug("Using template file: {}", file.getAbsolutePath());
                return String.join("\n", Files.readAllLines(Paths.get(file.getAbsolutePath())));
            }
            LOGGER.debug("Will try to read file: {} from embedded resources", fileName);
            return Resource.readTestFileContent(fileName.startsWith("/") ? fileName : ("/" + fileName));
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    format("No template could be found for templateIdentifier '%s'!", templateId));
        }
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
