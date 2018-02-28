package com.swisscom.cloud.sb.broker.config

import com.swisscom.cloud.sb.broker.model.repository.BaseRepositoryImpl
import groovy.transform.CompileStatic
import io.swagger.annotations.Api
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ImportResource
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@CompileStatic
@Configuration
@EnableJpaRepositories(value = 'com.swisscom.cloud.sb.broker.model.repository', repositoryBaseClass = BaseRepositoryImpl.class)
@ImportResource(value = 'classpath:beans.xml')
@EnableSwagger2
@EnableTransactionManagement
class ApplicationConfiguration {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(Api))
                .paths(PathSelectors.any())
                .build()
    }

}


