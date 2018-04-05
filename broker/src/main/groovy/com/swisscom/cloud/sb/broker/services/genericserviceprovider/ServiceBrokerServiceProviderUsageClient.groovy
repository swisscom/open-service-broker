package com.swisscom.cloud.sb.broker.services.genericserviceprovider

import com.swisscom.cloud.sb.broker.config.ApplicationUserConfig
import com.swisscom.cloud.sb.broker.config.UserConfig
import com.swisscom.cloud.sb.broker.config.WebSecurityConfig
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.client.ServiceBrokerClientExtended
import com.swisscom.cloud.sb.model.usage.ServiceUsage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.DefaultResponseErrorHandler
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
    public ServiceBrokerServiceProviderUsageClient(String baseUrl, String username, String password, ServiceBrokerClientExtended serviceBrokerClientExtended) {
        this.baseUrl = baseUrl
        this.username = username
        this.password = password
        this.serviceBrokerClientExtended = serviceBrokerClientExtended
    }

    ServiceUsage getLatestServiceInstanceUsage(String serviceInstanceId) {
        if(serviceBrokerClientExtended == null) {
            serviceBrokerClientExtended = createServiceBrokerClient(CustomServiceBrokerServiceProviderUsageErrorHandler.class)
        }
        def serviceUsage = serviceBrokerClientExtended.getUsage(serviceInstanceId)
        return new ServiceUsage()
    }

    ServiceBrokerClientExtended createServiceBrokerClient(Class errorHandler) {
        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory())
        restTemplate.setErrorHandler(errorHandler.newInstance())
        return new ServiceBrokerClientExtended(restTemplate, baseUrl, username, password, cfExtUser.username, cfExtUser.password)
    }

    // TODO: Add respective errors to error file and change them accordingly or not have custom error handler?
    private class CustomServiceBrokerServiceProviderUsageErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            if (response.statusCode == HttpStatus.BAD_REQUEST) {
                ErrorCode.SERVICEBROKERSERVICEPROVIDER_BINDING_BAD_REQUEST.throwNew()
            } else if (response.statusCode == HttpStatus.CONFLICT) {
                ErrorCode.SERVICEBROKERSERVICEPROVIDER_BINDING_CONFLICT.throwNew()
            } else if (response.statusCode == HttpStatus.UNPROCESSABLE_ENTITY) {
                ErrorCode.SERVICEBROKERSERVICEPROVIDER_BINDING_UNPROCESSABLE_ENTITY.throwNew()
            } else {
                ErrorCode.SERVICEBROKERSERVICEPROVIDER_INTERNAL_SERVER_ERROR.throwNew()
            }
            super.handleError(response)
        }
    }
}
