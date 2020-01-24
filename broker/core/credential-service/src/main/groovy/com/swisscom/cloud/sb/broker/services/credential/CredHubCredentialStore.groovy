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

import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.services.credhub.CredHubService
import com.swisscom.cloud.sb.broker.util.JsonHelper
import groovy.transform.CompileStatic

import static com.google.common.base.Preconditions.checkArgument
import static org.apache.commons.lang3.StringUtils.isNotBlank

/**
 * A {@link CredentialStore} which uses <a href='https://github.com/cloudfoundry-incubator/credhub'>CredHub</a> as
 * underlying store.
 * <p>
 *     For using it as {@link CredentialStore} of the system, include the following property in your configuration:
 *  <pre>osb.credential.store="credhub"</pre>
 *  or in yaml notation:
 *  <pre>
 *      osb:
 *          credential:
 *              store: "credhub"
 *      </pre>
 * </p>
 *
 */
@CompileStatic
class CredHubCredentialStore implements CredentialStore {

    private CredHubService credHubService

    public static CredHubCredentialStore of(CredHubService credHubService) {
        return new CredHubCredentialStore(credHubService);
    }

    private CredHubCredentialStore(CredHubService credHubService) {
        this.credHubService = credHubService
    }

    @Override
    ServiceBinding save(ServiceBinding key, String credentialJson) {
        checkArgument(isNotBlank(key.getGuid()), "ServiceBinding key must not be empty")
        checkArgument(isNotBlank(credentialJson),
                      "Credential for ServiceBinding '" + key.getGuid() + "' must not be empty")
        Map credentials = JsonHelper.parse(credentialJson, Map) as Map
        def credhubJsonCredential = credHubService.writeCredential(key.getGuid(), credentials)
        key.credhubCredentialId = credhubJsonCredential.getId()
        key.credentials = null
        return key
    }

    @Override
    ServiceBinding delete(ServiceBinding key) {
        credHubService.deleteCredential(key.guid)
        key.setCredhubCredentialId(null)
        return key
    }

    @Override
    String get(ServiceBinding key) {
        JsonHelper.toJsonString(credHubService.getCredential(key.credhubCredentialId).value)
    }

}
