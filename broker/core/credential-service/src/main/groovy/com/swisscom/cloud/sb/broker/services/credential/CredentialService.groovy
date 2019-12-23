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

package com.swisscom.cloud.sb.broker.services.credential

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.swisscom.cloud.sb.broker.model.ServiceBinding
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

import static com.google.common.base.Preconditions.checkArgument

@Component
class CredentialService {
    private static Logger LOGGER = LoggerFactory.getLogger(CredentialService.class)

    private CredentialStore credentialStore
    private final static objectReader = new ObjectMapper().readerFor(Map.class)

    CredentialService(CredentialStore credentialStore) {
        this.credentialStore = credentialStore
    }

    def writeCredential(ServiceBinding serviceBinding, String credentialJson) {
        checkArgument(serviceBinding != null, "service binding must not be null")
        checkArgument(credentialJson != null, "credential must not be null")
        checkArgument(isValidJson(serviceBinding, credentialJson), "credential must be valid json")
        credentialStore.save(serviceBinding, credentialJson)
    }

    def deleteCredential(ServiceBinding serviceBinding) {
        checkArgument(serviceBinding != null, "service binding must not be null")
        credentialStore.delete(serviceBinding)
    }

    String getCredential(ServiceBinding serviceBinding) {
        checkArgument(serviceBinding != null, "service binding must not be null")
        credentialStore.get(serviceBinding)
    }

    private static boolean isValidJson(ServiceBinding serviceBinding, String json) {
        try {
            objectReader.readTree(json)
            return true
        } catch (JsonParseException e) {
            LOGGER.error("Credential to be saved in ServiceBinding '{}' is not a valid JSON string!", serviceBinding)
            return false
        }
    }
}
