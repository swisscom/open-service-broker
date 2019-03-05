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

package com.swisscom.cloud.sb.broker

import com.swisscom.cloud.sb.broker.config.StandAloneConfigurationInitializer
import com.swisscom.cloud.sb.broker.config.WebContainerConfigurationInitializer
import groovy.transform.CompileStatic
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.retry.annotation.EnableRetry

@SpringBootApplication(exclude = [
	org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,
	org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
])
@CompileStatic
@EnableRetry
class ServiceBroker extends org.springframework.boot.web.servlet.support.SpringBootServletInitializer {
    static void main(String[] args) {
        new SpringApplicationBuilder(ServiceBroker.class)
                .initializers(new StandAloneConfigurationInitializer()).run(args)
    }

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		logger.info("Initializing service broker")
		//Needed for war based deployment
		application.initializers(new WebContainerConfigurationInitializer()).sources(ServiceBroker).profiles("influx")
	}
}
