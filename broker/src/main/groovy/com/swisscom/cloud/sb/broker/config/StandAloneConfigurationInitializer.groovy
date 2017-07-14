package com.swisscom.cloud.sb.broker.config

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.boot.context.config.ConfigFileApplicationListener
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.PropertySource
import org.springframework.core.io.FileSystemResourceLoader
import org.springframework.core.io.Resource

import java.util.function.Consumer

@CompileStatic
@Slf4j
class StandAloneConfigurationInitializer implements ApplicationContextInitializer {

    void initialize(ConfigurableApplicationContext applicationContext) {
        String filePath = System.getProperty(ConfigFileApplicationListener.CONFIG_LOCATION_PROPERTY)
        if (!filePath) {
            log.warn("'spring.config.location' is not configured. If service broker is running with the embedded tomcat for overriding configuration please specify a configuration yml path e.g. java -Dspring.config.location=file:/tmp/test.yml -jar servicebroker.jar")
            return
        }

        loadResource(filePath).ifPresent(new Consumer<Resource>() {
            @Override
            void accept(Resource resource) {
                applicationContext.getEnvironment().getPropertySources().addFirst(loadYamlResource(resource))
            }
        })
    }

    static PropertySource loadYamlResource(Resource resource) {
        YamlPropertySourceLoader sourceLoader = new YamlPropertySourceLoader()
        PropertySource<?> yamlProperties = sourceLoader.load("yamlProperties", resource, null)
        return yamlProperties
    }

    static Optional<Resource> loadResource(String filePath) {
        try {
            log.warn('Will try to load config from: ' + filePath)
            return Optional.of(new FileSystemResourceLoader().getResource(filePath))
        } catch (Exception e) {
            log.error('Resource loading failed', e)
            return Optional.empty()
        }
    }
}
