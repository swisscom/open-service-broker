package com.swisscom.cloud.sb.broker

import com.swisscom.cloud.sb.broker.config.StandAloneConfigurationInitializer
import com.swisscom.cloud.sb.broker.config.WebContainerConfigurationInitializer
import groovy.transform.CompileStatic
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.support.SpringBootServletInitializer

@SpringBootApplication(exclude = [org.springframework.cloud.servicebroker.config.ServiceBrokerAutoConfiguration.class,
        MongoAutoConfiguration.class, MongoDataAutoConfiguration.class])
@CompileStatic
class ServiceBroker extends SpringBootServletInitializer {
	static void main(String[] args) {
		new SpringApplicationBuilder(ServiceBroker.class)
				.initializers(new StandAloneConfigurationInitializer()).run(args)
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		logger.info("Initializing service broker")
		//Needed for war based deployment
		application.initializers(new WebContainerConfigurationInitializer()).sources(ServiceBroker)
	}
}