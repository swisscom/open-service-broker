package com.swisscom.cloud.sb.broker.services.kubernetes.templates

import com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis.config.KubernetesRedisConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.comparator.NumberPrefixedStringComparator
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.nio.file.Files
import java.nio.file.Paths
import java.util.regex.Matcher
import java.util.regex.Pattern

@Log4j
@Component
@CompileStatic
class KubernetesTemplateManager {

    private final KubernetesRedisConfig kubernetesConfig

    @Autowired
    KubernetesTemplateManager(KubernetesRedisConfig kubernetesConfig) {
        this.kubernetesConfig = kubernetesConfig
    }

    List<KubernetesTemplate> getTemplates() {
        List<KubernetesTemplate> templates = new LinkedList<>();
        for (String name : getTemplatesFilesNames()) {
            updateTemplates(name, templates)
        }
        return templates
    }

    private void updateTemplates(String name, List<KubernetesTemplate> templates) {
        String[] contents = (new String(Files.readAllBytes(Paths.get(kubernetesConfig.getKubernetesRedisV1TemplatesPath() + name)))).split("---")
        for (String content : contents) {
            templates.add(new KubernetesTemplate(content))
        }
    }

    private String[] getTemplatesFilesNames() {
        String[] files = (new File(kubernetesConfig.getKubernetesRedisV1TemplatesPath())).list()
        if (files.size() > 0) {
            Arrays.sort(files, new NumberPrefixedStringComparator())
            return files
        }
        throw new RuntimeException("Missing Kubernetes templates in " + kubernetesConfig.getKubernetesRedisV1TemplatesPath())
    }

}
