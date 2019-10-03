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

package com.swisscom.cloud.sb.broker.servicedefinition.dto

import com.swisscom.cloud.sb.broker.model.Parameter
import groovy.transform.CompileStatic

@CompileStatic
class ParameterDto implements Serializable {
    String template
    String name
    String value

    @Override
    public String toString() {
        return new StringJoiner(" ", ParameterDto.class.getSimpleName() + "[", "]")
                .add("'" + name + "' = '" + value + "'")
                .add(template != null ? "template='" + template + "'" : "")
                .toString();
    }

    ParameterDto() {}
    ParameterDto(Parameter parameter) {
        this.template = parameter.template
        this.name = parameter.name
        this.value = parameter.value
    }
}
