package com.swisscom.cloud.sb.broker.services.bosh;

import com.swisscom.cloud.sb.broker.config.Config;

public interface BoshConfig extends Config {

    String getBoshDirectorBaseUrl();

    String getBoshDirectorUsername();

    String getBoshDirectorPassword();
}
