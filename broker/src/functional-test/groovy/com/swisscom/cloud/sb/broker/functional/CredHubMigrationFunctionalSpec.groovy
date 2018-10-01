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

import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.services.credhub.CredHubMigrationInitializer
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.IgnoreIf

@IgnoreIf({ !CredHubBindingParametersFunctionalSpec.checkCredHubConfigSet() })
class CredHubMigrationFunctionalSpec extends BaseFunctionalSpec {

    @Autowired
    ServiceBindingRepository serviceBindingRepository

    @Autowired
    CredHubMigrationInitializer credHubMigrationInitializer

    def 'Migration of Binding updates Binding table'() {
        given:
            ServiceBinding serviceBinding = new ServiceBinding(
                    credentials: "{\"username\":\"test\",\"password\":\"password\"}",
                    guid: UUID.randomUUID().toString()
            )
            serviceBindingRepository.save(serviceBinding)

        when:
            credHubMigrationInitializer.storeCredHubCredential()

        then:
            ServiceBinding updatedServiceBinding = serviceBindingRepository.findById(serviceBinding.id).get()
            updatedServiceBinding.credhubCredentialId != null
            updatedServiceBinding.guid == serviceBinding.guid
            updatedServiceBinding.credentials == null

        cleanup:
            serviceBindingRepository.delete(updatedServiceBinding)
    }
}
