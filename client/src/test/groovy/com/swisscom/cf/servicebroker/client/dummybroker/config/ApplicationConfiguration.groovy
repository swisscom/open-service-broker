package com.swisscom.cf.servicebroker.client.dummybroker.config

import org.springframework.cloud.servicebroker.model.BrokerApiVersion
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationConfiguration {

    @Bean
    public BrokerApiVersion brokerApiVersion(){
        new BrokerApiVersion()
    }
}
