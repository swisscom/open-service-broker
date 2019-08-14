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

import com.swisscom.cloud.sb.broker.repository.BaseRepositoryImpl
import com.swisscom.cloud.sb.broker.util.LogContextEnrichInterceptor
import groovy.transform.CompileStatic
import io.swagger.annotations.Api
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ImportResource
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@CompileStatic
@Configuration
@EnableJpaRepositories(value = 'com.swisscom.cloud.sb.broker.repository', repositoryBaseClass = BaseRepositoryImpl.class)
@ImportResource(value = 'classpath:beans.xml')
@EnableSwagger2
@EnableTransactionManagement
class ApplicationConfiguration implements WebMvcConfigurer {

    @Bean
    Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(Api))
                .paths(PathSelectors.any())
                .build()
    }

    @Override
    void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LogContextEnrichInterceptor())
    }
}


