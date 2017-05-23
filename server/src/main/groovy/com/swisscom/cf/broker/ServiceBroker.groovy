package com.swisscom.cf.broker

import com.swisscom.cf.broker.config.WebContainerConfigurationInitializer
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.support.SpringBootServletInitializer

@SpringBootApplication(exclude = org.springframework.cloud.servicebroker.config.ServiceBrokerAutoConfiguration.class)
class ServiceBroker extends SpringBootServletInitializer {
	static void main(String[] args) {
        SpringApplication.run(ServiceBroker.class, args)
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		logger.info("Initializing service broker")
		//Needed for war based deployment
		application.initializers(new WebContainerConfigurationInitializer()).sources(ServiceBroker)
	}
}