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

package com.swisscom.cloud.sb.client.dummybroker.service

import groovy.transform.CompileStatic
import org.springframework.cloud.servicebroker.model.instance.*
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
@CompileStatic
class ServiceInstanceService implements org.springframework.cloud.servicebroker.service.ServiceInstanceService{

    @Override
    Mono<CreateServiceInstanceResponse> createServiceInstance(CreateServiceInstanceRequest request) {
        return Mono.just(CreateServiceInstanceResponse.builder().async(request.asyncAccepted).build())
    }

    @Override
    Mono<GetLastServiceOperationResponse> getLastOperation(GetLastServiceOperationRequest request) {
        return Mono.just(GetLastServiceOperationResponse
                .builder()
                .operationState(OperationState.SUCCEEDED)
                .description('some description')
                .build())
    }

    @Override
    Mono<DeleteServiceInstanceResponse> deleteServiceInstance(DeleteServiceInstanceRequest request) {
        return Mono.just(DeleteServiceInstanceResponse.builder().async(false).build())
    }

    @Override
    Mono<UpdateServiceInstanceResponse> updateServiceInstance(UpdateServiceInstanceRequest request) {
        return Mono.just(UpdateServiceInstanceResponse.builder().async(false).build())
    }
}
