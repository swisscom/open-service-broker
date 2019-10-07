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

package com.swisscom.cloud.sb.broker.util

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.ValidationMessage

final class JsonSchemaHelper {
    static final String JSON_SCHEMA_V4_FILENAME = 'json_schema_v4.json'

    static JsonSchema getJsonSchema(String filename) throws Exception {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance()
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename)
        factory.getSchema(is)
    }

    static JsonNode getJsonNodeFromStringContent(String content) throws Exception {
        ObjectMapper mapper = new ObjectMapper()
        JsonNode node = mapper.readTree(content)
        return node
    }

    static Set<ValidationMessage> validateJson(String json, String schemaFilename = JSON_SCHEMA_V4_FILENAME) {
        JsonSchema schema = getJsonSchema(schemaFilename)
        JsonNode node = getJsonNodeFromStringContent(json)
        schema.validate(node)
    }
}
