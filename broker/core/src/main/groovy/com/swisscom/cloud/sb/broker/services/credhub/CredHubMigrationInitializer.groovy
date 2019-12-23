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

package com.swisscom.cloud.sb.broker.services.credhub


import com.swisscom.cloud.sb.broker.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.services.credential.CredentialStore
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import javax.annotation.PostConstruct


@Component
@EnableConfigurationProperties
@Slf4j
@Transactional
@ConditionalOnProperty(name = "osb.credential.store", havingValue = "credhub")
class CredHubMigrationInitializer {

    @Autowired
    private ServiceBindingRepository serviceBindingRepository

    @Autowired
    private CredentialStore credentialStore

    @PostConstruct
    void init() throws Exception {
        storeCredHubCredential()
    }

    void storeCredHubCredential() {
        def bindings = serviceBindingRepository.findNotMigratedCredHubBindings()
        log.info("Starting ServiceBinding credential migration to CredHub. Found: ${bindings?.size()} bindings to migrate.")
        bindings.each {
            it ->
                def serviceBinding = it
                credentialStore.save(serviceBinding, serviceBinding.credentials)
                serviceBindingRepository.save(serviceBinding)
        }
        serviceBindingRepository.flush()
    }

}
