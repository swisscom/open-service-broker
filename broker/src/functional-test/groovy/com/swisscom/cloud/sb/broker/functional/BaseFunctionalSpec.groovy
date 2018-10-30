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

package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.config.ApplicationUserConfig
import com.swisscom.cloud.sb.broker.config.UserConfig
import com.swisscom.cloud.sb.broker.config.WebSecurityConfig
import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.util.ServiceLifeCycler
import com.swisscom.cloud.sb.client.ServiceBrokerClientExtended
import com.swisscom.cloud.sb.client.model.LastOperationState
import groovy.transform.CompileStatic
import org.joda.time.LocalTime
import org.joda.time.Seconds
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.client.RestTemplate
import org.springframework.web.context.WebApplicationContext
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

@CompileStatic
@Stepwise
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration
abstract class BaseFunctionalSpec extends Specification {

    @Autowired
    protected WebApplicationContext applicationContext

    @Autowired
    protected TestHelperService testHelperService

    @Shared
    protected ServiceLifeCycler serviceLifeCycler
    @Shared
    protected ServiceBrokerClientExtended serviceBrokerClient

    @Shared
    boolean initialized

    protected String appBaseUrl = 'http://localhost:8080'

    protected String serviceDefinitionUrl = appBaseUrl + "/service-definition/{id}"

    @Autowired
    private ApplicationUserConfig userConfig
    @Shared
    protected UserConfig cfAdminUser
    @Shared
    protected UserConfig cfExtUser

    @Autowired
    void init(ServiceLifeCycler serviceLifeCycler) {
        if (!initialized) {
            cfAdminUser = getUserByRole(WebSecurityConfig.ROLE_CF_ADMIN)
            cfExtUser = getUserByRole(WebSecurityConfig.ROLE_CF_EXT_ADMIN)

            this.serviceLifeCycler = serviceLifeCycler
            serviceBrokerClient = new ServiceBrokerClientExtended(new RestTemplate(), appBaseUrl, cfAdminUser.username, cfAdminUser.password, cfExtUser.username, cfExtUser.password)
            initialized = true
        }
    }

    /**
     * Find first occurrence of a UserConfig with a requested role across all guids.
     * @param role
     * @return
     */
    protected UserConfig getUserByRole(String role) {
        return userConfig.platformUsers.find { it.role == role }
    }

    void waitUntilMaxTimeOrTargetState(String serviceInstanceGuid, int seconds) {
        int sleepTime = 1000
        def start = LocalTime.now();

        if (seconds > 0) {
            while (start.plusSeconds(seconds).isAfter(LocalTime.now())) {
                def timeUntilForcedExecution = Seconds.secondsBetween(LocalTime.now(), start.plusSeconds(seconds)).getSeconds()
                if (timeUntilForcedExecution % 4 == 0) {
                    def lastOperationResponse = serviceBrokerClient.getServiceInstanceLastOperation(serviceInstanceGuid).getBody();
                    def operationState = lastOperationResponse.state
                    if (operationState == LastOperationState.SUCCEEDED || operationState == LastOperationState.FAILED)
                        return
                }

                println("Execution continues in ${timeUntilForcedExecution} second(s)")
                Thread.sleep(sleepTime)
            }
        }
    }

    protected String readResourceContent(String resourcePath) {
        return new File(getClass().getResource(resourcePath).getFile()).text
    }
}
