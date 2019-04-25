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

/**
 * A repository for storing authentication credentials for {@link ServiceBinding}.
 * <p>
 *     Each broker system needs some place where the different credentials needed by applications for using certain
 * {@link com.swisscom.cloud.sb.broker.model.ServiceInstance} are persisted. The {@link CredentialStore} persist the
 *     credentials (storing them as Json strings) using the {@link ServiceBinding} as key for retrieving them.
 * </p>
 * <p>
 *     Depending in the service to which they are referring, we could have different types of credentials, so in
 *     general, it is better to consider that the credentials stored in this {@link CredentialStore} are a map of
 *     objects defining the different types of user names, roles, and/or properties that constitute a credential in
 *     certain service.
 * </p>
 */
interface CredentialStore {

    /**
     * Save the credentials needed for using certain service specified as a {@link ServiceBinding}
     * @param key the {@link ServiceBinding} that points to a service where this credentials are valid
     * @param credentialJson a map of objects defining credentials codified as a JSON string
     */
    def save(ServiceBinding key, String credentialJson)

    /**
     * Delete the credentials associated to the passed {@link ServiceBinding}
     * @param key
     */
    def delete(ServiceBinding key)

    /**
     * Returns a map of objects constituting the credentials available for certain {@link ServiceBinding} codified as
     * JSON string.
     * @param key
     * @return a map of objects representing credentials codified in a JSON String
     */
    String get(ServiceBinding key)

}
