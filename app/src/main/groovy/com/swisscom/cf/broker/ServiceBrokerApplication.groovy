package com.swisscom.cf.broker

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.support.SpringBootServletInitializer

@SpringBootApplication(exclude = org.springframework.cloud.servicebroker.config.ServiceBrokerAutoConfiguration.class)
class ServiceBrokerApplication extends SpringBootServletInitializer {
	static void main(String[] args) {
        SpringApplication.run(ServiceBrokerApplication.class, args)
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		//Needed for war based deployment
		application.sources(ServiceBrokerApplication)
	}
}