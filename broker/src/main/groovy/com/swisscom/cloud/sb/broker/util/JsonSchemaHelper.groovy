package com.swisscom.cloud.sb.broker.util

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.ValidationMessage

abstract class JsonSchemaHelper {
    static
    final String JSON_SCHEMA_V4_FILENAME = 'json_schema_v4.json'

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
