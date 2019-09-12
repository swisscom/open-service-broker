package com.swisscom.cloud.sb.broker.services.bosh.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//TODO At the moment is instantiating a WebClient inside BoshWebClient with an unstrusted SSL Context. This is why I don't want to inject it
@Configuration
public class BoshWebClientConfiguration {

    @Bean
    public BoshWebClient boshWebClient(BoshDirectorProperties boshDirectorProperties) {
        return BoshWebClient.boshWebClient(boshDirectorProperties.getBoshBaseUrl(),
                                           boshDirectorProperties.getBoshDirectorUsername(),
                                           boshDirectorProperties.getBoshDirectorPassword());
    }


}
