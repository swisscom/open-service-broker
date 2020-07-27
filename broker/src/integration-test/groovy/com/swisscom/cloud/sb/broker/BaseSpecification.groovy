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

package com.swisscom.cloud.sb.broker

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration
@ActiveProfiles(["default","test","secrets"])
@SpringBootTest(properties = "spring.autoconfigure.exclude=com.swisscom.cloud.sb.broker.util.httpserver.WebSecurityConfig,org.springframework.cloud.servicebroker.autoconfigure.web.ServiceBrokerAutoConfiguration,org.springframework.cloud.servicebroker.autoconfigure.web.servlet.ServiceBrokerWebMvcAutoConfiguration")
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASPECTJ, pattern = "com.swisscom.cloud.sb.broker.util.httpserver.*"))
abstract class BaseSpecification extends Specification {
}
