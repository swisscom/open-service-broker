package com.swisscom.cloud.sb.broker.services.bosh.client;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;

@Component
@ConfigurationProperties(prefix = "com.swisscom.cloud.sb.broker.services.bosh.client")
public class BoshDirectorProperties {

    @NotBlank
    private String boshBaseUrl;

    @NotBlank
    private String boshDirectorUsername;

    @NotBlank
    private String boshDirectorPassword;

    public String getBoshBaseUrl() {
        return boshBaseUrl;
    }

    public void setBoshBaseUrl(String boshBaseUrl) {
        this.boshBaseUrl = boshBaseUrl;
    }

    public String getBoshDirectorUsername() {
        return boshDirectorUsername;
    }

    public void setBoshDirectorUsername(String boshDirectorUsername) {
        this.boshDirectorUsername = boshDirectorUsername;
    }

    public char[] getBoshDirectorPassword() {
        return boshDirectorPassword.toCharArray();
    }

    public void setBoshDirectorPassword(String boshDirectorPassword) {
        this.boshDirectorPassword = boshDirectorPassword;
    }
}
