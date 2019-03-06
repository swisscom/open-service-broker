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

package com.swisscom.cloud.sb.broker.services.bosh.client

import com.swisscom.cloud.sb.broker.BaseSpecification
import com.swisscom.cloud.sb.broker.services.bosh.dto.BoshConfigRequestDto
import com.swisscom.cloud.sb.broker.services.bosh.dto.BoshConfigResponseDto
import com.swisscom.cloud.sb.broker.util.DummyConfig
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore
import spock.lang.Stepwise

@Ignore
@Stepwise
class BoshClientTest extends BaseSpecification {
    @Autowired
    BoshClientFactory boshClientFactory
    BoshConfigRequestDto requestConfig

    def setup() {
        requestConfig = new BoshConfigRequestDto(name: 'boshClientTest', type: 'cloud', content: '--- {}')
    }

    def "upload cloud config"() {
        when:
        createClient().setConfig(requestConfig)
        then:
        noExceptionThrown()
    }

    def "get uploaded cloud config"() {
        when:
        BoshConfigResponseDto getConfig = createClient().getConfigs(requestConfig.name, requestConfig.type)[0]
        then:
        getConfig.name == requestConfig.name
        getConfig.type == requestConfig.type
        getConfig.content == requestConfig.content
    }

    def "delete cloud config"() {
        when:
        createClient().deleteConfig(requestConfig.name, requestConfig.type)
        then:
        noExceptionThrown()
    }

    def "get deleted cloud config"() {
        when:
        List<BoshConfigResponseDto> configs = createClient().getConfigs(requestConfig.name, requestConfig.type)
        then:
        configs.size() == 0
    }

    def createClient() {
        return boshClientFactory.build(new DummyConfig(boshDirectorBaseUrl: "https://192.168.50.6:25555",
                boshDirectorUsername: "admin",
                boshDirectorPassword: "jiuoqjhh9gt4c9rj5qky"))
    }

}
