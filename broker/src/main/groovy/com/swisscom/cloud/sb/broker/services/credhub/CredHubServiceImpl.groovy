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

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.credhub.core.CredHubOperations
import org.springframework.credhub.support.CredentialDetails
import org.springframework.credhub.support.SimpleCredentialName
import org.springframework.credhub.support.json.JsonCredential
import org.springframework.credhub.support.json.JsonCredentialRequest

@CompileStatic
@Slf4j
class CredHubServiceImpl implements CredHubService {

    @Autowired
    private CredHubOperations credHubOperations

    @Override
    CredentialDetails<JsonCredential> writeCredential(String credentialName, Map<String, String> credentials) {
        log.info("Writing new CredHub Credential for name: ${credentialName}")
        JsonCredential jsonCredential = new JsonCredential(credentials)
        JsonCredentialRequest request =
                JsonCredentialRequest.builder()
                        .overwrite(true)
                        .name(new SimpleCredentialName('/' + credentialName))
                        .value(jsonCredential)
                        .build()
        credHubOperations.write(request)
    }

    @Override
    CredentialDetails<JsonCredential> getCredential(String id) {
        log.info("Get CredHub credentials for id: ${id}")
        credHubOperations.getById(id, JsonCredential)
    }

    @Override
    void deleteCredential(String credentialName) {
        log.info("Delete CredHub credentials for name: ${credentialName}")
        credHubOperations.deleteByName(new SimpleCredentialName('/' + credentialName))
    }

}
