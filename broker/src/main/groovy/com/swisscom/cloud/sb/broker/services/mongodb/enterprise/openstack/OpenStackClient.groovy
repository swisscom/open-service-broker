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

package com.swisscom.cloud.sb.broker.services.mongodb.enterprise.openstack

import com.google.common.base.Optional
import org.openstack4j.api.OSClient
import org.openstack4j.core.transport.Config
import org.openstack4j.model.compute.ServerGroup
import org.openstack4j.openstack.OSFactory

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession


class OpenStackClient {
    public static final String POLICY_AFFINITY = "affinity"
    public static final String POLICY_ANTI_AFFINITY = "anti-affinity"

    private final String url
    private final String username
    private final String password
    private final String tenantName


    OpenStackClient(String url, String username, String password, String tenant) {
        this.url = url
        this.username = username
        this.password = password
        this.tenantName = tenant
    }

    ServerGroup createServerGroup(String name, String policy) {
        def optionalServerGroup = findServerGroup(name)
        if (optionalServerGroup.present) {
            throw new RuntimeException("OpenStack ServerGroup with name:${name} already exists")
        }
        return createOSClient().compute().serverGroups().create(name, policy)
    }

    ServerGroup createAntiAffinityServerGroup(String name) {
        return createServerGroup(name, POLICY_ANTI_AFFINITY)
    }

    List<? extends ServerGroup> listServerGroups() {
        return createOSClient().
                compute().
                serverGroups().
                list()
    }

    Optional<? extends ServerGroup> findServerGroup(String name) {
        def list = listServerGroups().findAll { it.name == name }
        if (list.size() > 1) {
            throw new RuntimeException("Several OpenStack ServerGroups with name:${name} already exists")
        }
        return list ? Optional.of(list.get(0)) : Optional.absent()
    }

    def deleteServerGroup(String serverGroupId) {
        return createOSClient().
                compute().
                serverGroups().
                delete(serverGroupId)
    }

    private OSClient createOSClient() {
        return OSFactory.builder()
                .endpoint(url)
                .credentials(username, password)
                .tenantName(tenantName)
                .withConfig(Config.DEFAULT.withSSLVerificationDisabled()
                .withHostnameVerifier(new HostnameVerifier() {
            @Override
            boolean verify(String s, SSLSession sslSession) {
                return true
            }
        })).authenticate()
    }

}
