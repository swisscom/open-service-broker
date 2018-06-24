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

package com.swisscom.cloud.sb.broker.services.bosh

import com.swisscom.cloud.sb.broker.services.bosh.client.BoshClientFactory
import com.swisscom.cloud.sb.broker.services.common.TemplateConfig
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.openstack.OpenStackClientFactory
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
class BoshFacadeFactory {
    private final BoshClientFactory boshClientFactory
    private final OpenStackClientFactory openStackClientFactory
    private final BoshTemplateFactory boshTemplateFactory
    private final TemplateConfig templateConfig

    @Autowired
    BoshFacadeFactory(BoshClientFactory boshClientFactory, OpenStackClientFactory openStackClientFactory, BoshTemplateFactory boshTemplateFactory, TemplateConfig templateConfig) {
        this.boshClientFactory = boshClientFactory
        this.openStackClientFactory = openStackClientFactory
        this.boshTemplateFactory = boshTemplateFactory
        this.templateConfig = templateConfig
    }

    BoshFacade build(BoshBasedServiceConfig boshBasedServiceConfig) {
        return new BoshFacade(boshClientFactory, openStackClientFactory, boshBasedServiceConfig, boshTemplateFactory, templateConfig)
    }
}
