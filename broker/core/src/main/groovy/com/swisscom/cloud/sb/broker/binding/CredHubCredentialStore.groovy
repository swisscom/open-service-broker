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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

import static com.swisscom.cloud.sb.broker.util.JsonHelper.toJsonString

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
@Component
@CompileStatic
@Slf4j
@ConditionalOnProperty(name = "osb.credential.store", havingValue = "credhub")
class CredHubCredentialStore implements CredentialStore {

    @Autowired
    private ApplicationContext applicationContext

    @Autowired
    private CredHubService credHubService

    def save(ServiceBinding key, String credentialJson) {
        Map credentials = JsonHelper.parse(credentialJson, Map) as Map
        def credhubJsonCredential = credHubService.writeCredential(key.guid, credentials)
        key.credhubCredentialId = credhubJsonCredential.id
        key.credentials = null
    }

    def delete(ServiceBinding key) {
        credHubService.deleteCredential(key.guid)
    }

    String get(ServiceBinding key) {
        toJsonString(credHubService.getCredential(key.credhubCredentialId).value)
    }

}
