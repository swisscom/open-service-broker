package com.swisscom.cloud.sb.broker.config

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.PropertySource
import org.springframework.core.io.FileSystemResource

@CompileStatic
@Slf4j
class WebContainerConfigurationInitializer implements ApplicationContextInitializer {
    private static final String CONFIG_FILE = "/servicebroker.yml"
    private static final String CATALINA_HOME = "catalina.home"
    private static final String CATALINA_BASE = "catalina.base"
    private static final String FOLDER_CONFIG = "/conf"

    void initialize(ConfigurableApplicationContext applicationContext) {
        String filePath = getFilePathInTomcatConfigFolder(CONFIG_FILE)
        if(!filePath){
            log.warn("Please make sure that catalina.home or catalina.base is configured correctly.")
            return
        }

        final File file = new File(filePath)
        if(file.exists()){
            log.warn('Adding config location:'+ filePath)
            applicationContext.getEnvironment().getPropertySources().addFirst(loadYamlResource(file))
        }else{
            log.warn("Config does not exist at location:${filePath}")
        }
    }

    static PropertySource loadYamlResource(File file) {
        FileSystemResource resource = new FileSystemResource(file)
        YamlPropertySourceLoader sourceLoader = new YamlPropertySourceLoader()
        PropertySource<?> yamlProperties = sourceLoader.load("externalYamlProperties", resource, null)
        return yamlProperties
    }

    static String getFilePathInTomcatConfigFolder(String fileName){
        if(System.getProperty(CATALINA_BASE)){
            return System.getProperty(CATALINA_BASE) + FOLDER_CONFIG + fileName
        }

        if(System.getProperty(CATALINA_HOME)){
            return System.getProperty(CATALINA_HOME) + FOLDER_CONFIG + fileName
        }

        return null
    }
}
