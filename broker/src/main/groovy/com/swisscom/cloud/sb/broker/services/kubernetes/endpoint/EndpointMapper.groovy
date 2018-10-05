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

package com.swisscom.cloud.sb.broker.services.kubernetes.endpoint

import com.swisscom.cloud.sb.broker.services.kubernetes.dto.ConfigMapResponseDto
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.DeploymentResponseDto
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.NamespaceResponseDto
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.RolesResponseDto
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.ServiceAccountsResponseDto
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.ServiceResponse
import groovy.transform.CompileStatic
import org.springframework.data.util.Pair

@CompileStatic
enum EndpointMapper {
    INSTANCE

    private static final HashMap<String, Pair> mapper = new HashMap<>()

    Pair<String, ?> getEndpointUrlByType(String templateType) {
        if (mapper.containsKey(templateType))
            return mapper.get(templateType)
        throw new RuntimeException("Template type not found!" + templateType)
    }

    static {
        mapper.put("Namespace", Pair.of("/api/v1/namespaces", new NamespaceResponseDto()))
        mapper.put("ServiceAccount", Pair.of("/api/v1/namespaces/serviceInstanceGuid/serviceaccounts", new ServiceAccountsResponseDto()))
        mapper.put("Role", Pair.of("/apis/rbac.authorization.k8s.io/v1beta1/namespaces/serviceInstanceGuid/roles", new RolesResponseDto()))
        mapper.put("RoleBinding", Pair.of("/apis/rbac.authorization.k8s.io/v1beta1/namespaces/serviceInstanceGuid/rolebindings", new Object()))
        mapper.put("ConfigMap", Pair.of("/api/v1/namespaces/serviceInstanceGuid/configmaps", new ConfigMapResponseDto()))
        mapper.put("Service", Pair.of("/api/v1/namespaces/serviceInstanceGuid/services", new ServiceResponse()))
        mapper.put("DeploymentDto", Pair.of("/apis/apps/v1beta1/namespaces/serviceInstanceGuid/deployments", new DeploymentResponseDto()))
    }


}
