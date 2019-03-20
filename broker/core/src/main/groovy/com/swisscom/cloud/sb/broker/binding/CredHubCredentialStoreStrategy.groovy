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

package com.swisscom.cloud.sb.broker.binding

import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.services.credhub.CredHubService
import com.swisscom.cloud.sb.broker.util.JsonHelper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
@CompileStatic
@Slf4j
class CredHubCredentialStoreStrategy implements CredentialStoreStrategy {

    @Autowired
    private ApplicationContext applicationContext

    @Autowired(required = false)
    private CredHubService credHubService

    def writeCredential(ServiceBinding serviceBinding, String credentialJson) {
        Map credentials = JsonHelper.parse(credentialJson, Map) as Map
        def credhubJsonCredential = credHubService.writeCredential(serviceBinding.guid, credentials)
        serviceBinding.credhubCredentialId = credhubJsonCredential.id
        serviceBinding.credentials = null
    }

    def deleteCredential(ServiceBinding serviceBinding) {
        credHubService.deleteCredential(serviceBinding.guid)
    }

    String getCredential(ServiceBinding serviceBinding) {
        JsonHelper.toJsonString(credHubService.getCredential(serviceBinding.credhubCredentialId).value)
    }

    boolean isCredHubServiceAvailable() {
        return credHubService != null
    }

}
