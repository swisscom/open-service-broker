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

package com.swisscom.cloud.sb.broker.servicedefinition.converter

import com.swisscom.cloud.sb.broker.converter.AbstractGenericConverter
import com.swisscom.cloud.sb.broker.model.Parameter
import com.swisscom.cloud.sb.broker.servicedefinition.dto.ParameterDto
import org.springframework.stereotype.Component

@Component
class ParameterDtoConverter extends AbstractGenericConverter<Parameter, ParameterDto> {
    @Override
    protected void convert(Parameter source, ParameterDto prototype) {
        prototype.name = source.name
        prototype.value = source.value
        prototype.template = source.template
    }
}
