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

package com.swisscom.cloud.sb.broker.cfapi.converter

import com.swisscom.cloud.sb.broker.binding.CredentialService
import com.swisscom.cloud.sb.broker.binding.ServiceInstanceBindingResponseDto
import com.swisscom.cloud.sb.broker.converter.AbstractGenericConverter
import com.swisscom.cloud.sb.broker.model.ServiceBinding
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Slf4j
@Component
class ServiceInstanceBindingDtoConverter extends AbstractGenericConverter<ServiceBinding, ServiceInstanceBindingResponseDto> {

    @Autowired
    private CredentialService credentialService

    @Override
    void convert(ServiceBinding source, ServiceInstanceBindingResponseDto prototype) {
        prototype.credentials = credentialService.getCredential(source)
        prototype.parameters = source.parameters
        prototype.details = source.details
        prototype.routeServiceUrl = null
        prototype.syslogDrainUrl = null
        prototype.volumeMounts = null
    }

}