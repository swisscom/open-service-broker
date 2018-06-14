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

package com.swisscom.cloud.sb.broker.services.genericserviceprovider

import com.swisscom.cloud.sb.broker.config.ApplicationUserConfig
import com.swisscom.cloud.sb.broker.config.UserConfig
import com.swisscom.cloud.sb.broker.config.WebSecurityConfig
import com.swisscom.cloud.sb.client.ServiceBrokerClientExtended
import com.swisscom.cloud.sb.model.usage.ServiceUsage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

class ServiceBrokerServiceProviderUsageClient {

    private final String baseUrl;
    private final String username;
    private final String password;

    ApplicationUserConfig userConfig
    private UserConfig cfExtUser

    private ServiceBrokerClientExtended serviceBrokerClientExtended

    @Autowired
    public ServiceBrokerServiceProviderUsageClient(String baseUrl, String username, String password, ApplicationUserConfig userConfig) {
        this.baseUrl = baseUrl
        this.username = username
        this.password = password
        this.userConfig = userConfig
        cfExtUser = getUserByRole(WebSecurityConfig.ROLE_CF_EXT_ADMIN)

    }

    protected UserConfig getUserByRole(String role) {
        return userConfig.platformUsers.find { it.role == role }
    }

    // For testing purposes so that a mock serviceBrokerClient can be provided
    ServiceBrokerServiceProviderUsageClient(String baseUrl, String username, String password, ApplicationUserConfig userConfig, ServiceBrokerClientExtended serviceBrokerClientExtended) {
        this.baseUrl = baseUrl
        this.username = username
        this.password = password
        this.userConfig = userConfig
        cfExtUser = getUserByRole(WebSecurityConfig.ROLE_CF_EXT_ADMIN)
        this.serviceBrokerClientExtended = serviceBrokerClientExtended
    }

    ServiceUsage getLatestServiceInstanceUsage(String serviceInstanceId) {
        if (serviceBrokerClientExtended == null) {
            serviceBrokerClientExtended = createServiceBrokerClient()
        }
        def serviceUsage = serviceBrokerClientExtended.getUsage(serviceInstanceId)
        return new ServiceUsage()
    }

    ServiceBrokerClientExtended createServiceBrokerClient() {
        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory())
        return new ServiceBrokerClientExtended(restTemplate, baseUrl, username, password, cfExtUser.username, cfExtUser.password)
    }
}
