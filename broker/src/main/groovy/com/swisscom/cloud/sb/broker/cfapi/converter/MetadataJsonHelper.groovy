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

import com.fasterxml.jackson.databind.ObjectMapper

class MetadataJsonHelper {

    static ObjectMapper _objectMapper

    static ObjectMapper getObjectMapper() {
        if (_objectMapper == null)
            _objectMapper = new ObjectMapper()

        return _objectMapper
    }

    static Object getValue(String type, String value) {
        try {
            return objectMapper.readValue(value, Class.forName(type))
        } catch (Exception ex) {
            if (!type) {
                return value
            }
            switch (type.toLowerCase()) {
                case boolean.class.name:
                case Boolean.class.name:
                case 'bool':
                case 'boolean':
                    return Boolean.valueOf(value)
                case int.class.name:
                case Integer.class.name:
                case 'int':
                case 'integer':
                    return Integer.valueOf(value)
                case long.class.name:
                case Long.class.name:
                case 'long':
                case 'Long':
                    return Long.valueOf(value)
                case ArrayList.class.name:

                default:
                    return value
            }
        }
    }
}
