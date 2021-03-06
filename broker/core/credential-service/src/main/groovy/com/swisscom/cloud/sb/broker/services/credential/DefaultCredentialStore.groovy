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
import groovy.transform.CompileStatic

/**
 * A simple implementation of a {@link CredentialStore}: uses {@link ServiceBinding#credentials} for storing the
 * credentials in JSON.
 * <p>
 * For using it as {@link CredentialStore} of the system, do not include anything in the configuration,
 * or include the following property in your configuration:
 *  <pre>osb.credential.store="default"</pre>
 *  or in yaml notation:
 *  <pre>
 *      osb:
 *          credential:
 *              store: "default"
 *      </pre>
 * </p>
 *
 * <h1>WARNING: INSECURE FOR PRODUCTION USE</h1>
 * It stores the credentials in string fields that should not be  used for storing secrets (char[] should be
 * used instead. It's main use is for testing without needing to prepare a suitable {@link CredentialStore}
 */
@CompileStatic
class DefaultCredentialStore implements CredentialStore {

    private DefaultCredentialStore() {}

    public static DefaultCredentialStore create() {
        return new DefaultCredentialStore()
    }

    @Override
    ServiceBinding save(ServiceBinding key, String credentialJson) {
        key.credentials = credentialJson
        return key
    }

    @Override
    ServiceBinding delete(ServiceBinding key) {
        key.credentials = null
        return key
    }

    @Override
    String get(ServiceBinding key) {
        return key.credentials
    }

}
