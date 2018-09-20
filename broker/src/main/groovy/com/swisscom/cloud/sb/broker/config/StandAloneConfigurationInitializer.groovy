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
        String filePath = System.getProperty(ConfigFileApplicationListener.CONFIG_ADDITIONAL_LOCATION_PROPERTY)
        if (!filePath) {
            log.warn("'spring.config.additional-location' is not configured. If service broker is running with the embedded tomcat for overriding configuration please specify a configuration yml path e.g. java -Dspring.config.additional-location=file:/tmp/test.yml -jar servicebroker.jar")
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
        List<PropertySource<?>> yamlProperties = sourceLoader.load("yamlProperties", resource)
        return yamlProperties.first()
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
